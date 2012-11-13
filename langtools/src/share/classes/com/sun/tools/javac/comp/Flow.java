/*
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

//todo: one might eliminate uninits.andSets when monotonic

package com.sun.tools.javac.comp;

import java.util.HashMap;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Flags.BLOCK;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTag.BOOLEAN;
import static com.sun.tools.javac.code.TypeTag.VOID;
import static com.sun.tools.javac.tree.JCTree.Tag.*;

/** This pass implements dataflow analysis for Java programs though
 *  different AST visitor steps. Liveness analysis (see AliveAlanyzer) checks that
 *  every statement is reachable. Exception analysis (see FlowAnalyzer) ensures that
 *  every checked exception that is thrown is declared or caught.  Definite assignment analysis
 *  (see AssignAnalyzer) ensures that each variable is assigned when used.  Definite
 *  unassignment analysis (see AssignAnalyzer) in ensures that no final variable
 *  is assigned more than once. Finally, local variable capture analysis (see CaptureAnalyzer)
 *  determines that local variables accessed within the scope of an inner class/lambda
 *  are either final or effectively-final.
 *
 *  <p>The JLS has a number of problems in the
 *  specification of these flow analysis problems. This implementation
 *  attempts to address those issues.
 *
 *  <p>First, there is no accommodation for a finally clause that cannot
 *  complete normally. For liveness analysis, an intervening finally
 *  clause can cause a break, continue, or return not to reach its
 *  target.  For exception analysis, an intervening finally clause can
 *  cause any exception to be "caught".  For DA/DU analysis, the finally
 *  clause can prevent a transfer of control from propagating DA/DU
 *  state to the target.  In addition, code in the finally clause can
 *  affect the DA/DU status of variables.
 *
 *  <p>For try statements, we introduce the idea of a variable being
 *  definitely unassigned "everywhere" in a block.  A variable V is
 *  "unassigned everywhere" in a block iff it is unassigned at the
 *  beginning of the block and there is no reachable assignment to V
 *  in the block.  An assignment V=e is reachable iff V is not DA
 *  after e.  Then we can say that V is DU at the beginning of the
 *  catch block iff V is DU everywhere in the try block.  Similarly, V
 *  is DU at the beginning of the finally block iff V is DU everywhere
 *  in the try block and in every catch block.  Specifically, the
 *  following bullet is added to 16.2.2
 *  <pre>
 *      V is <em>unassigned everywhere</em> in a block if it is
 *      unassigned before the block and there is no reachable
 *      assignment to V within the block.
 *  </pre>
 *  <p>In 16.2.15, the third bullet (and all of its sub-bullets) for all
 *  try blocks is changed to
 *  <pre>
 *      V is definitely unassigned before a catch block iff V is
 *      definitely unassigned everywhere in the try block.
 *  </pre>
 *  <p>The last bullet (and all of its sub-bullets) for try blocks that
 *  have a finally block is changed to
 *  <pre>
 *      V is definitely unassigned before the finally block iff
 *      V is definitely unassigned everywhere in the try block
 *      and everywhere in each catch block of the try statement.
 *  </pre>
 *  <p>In addition,
 *  <pre>
 *      V is definitely assigned at the end of a constructor iff
 *      V is definitely assigned after the block that is the body
 *      of the constructor and V is definitely assigned at every
 *      return that can return from the constructor.
 *  </pre>
 *  <p>In addition, each continue statement with the loop as its target
 *  is treated as a jump to the end of the loop body, and "intervening"
 *  finally clauses are treated as follows: V is DA "due to the
 *  continue" iff V is DA before the continue statement or V is DA at
 *  the end of any intervening finally block.  V is DU "due to the
 *  continue" iff any intervening finally cannot complete normally or V
 *  is DU at the end of every intervening finally block.  This "due to
 *  the continue" concept is then used in the spec for the loops.
 *
 *  <p>Similarly, break statements must consider intervening finally
 *  blocks.  For liveness analysis, a break statement for which any
 *  intervening finally cannot complete normally is not considered to
 *  cause the target statement to be able to complete normally. Then
 *  we say V is DA "due to the break" iff V is DA before the break or
 *  V is DA at the end of any intervening finally block.  V is DU "due
 *  to the break" iff any intervening finally cannot complete normally
 *  or V is DU at the break and at the end of every intervening
 *  finally block.  (I suspect this latter condition can be
 *  simplified.)  This "due to the break" is then used in the spec for
 *  all statements that can be "broken".
 *
 *  <p>The return statement is treated similarly.  V is DA "due to a
 *  return statement" iff V is DA before the return statement or V is
 *  DA at the end of any intervening finally block.  Note that we
 *  don't have to worry about the return expression because this
 *  concept is only used for construcrors.
 *
 *  <p>There is no spec in the JLS for when a variable is definitely
 *  assigned at the end of a constructor, which is needed for final
 *  fields (8.3.1.2).  We implement the rule that V is DA at the end
 *  of the constructor iff it is DA and the end of the body of the
 *  constructor and V is DA "due to" every return of the constructor.
 *
 *  <p>Intervening finally blocks similarly affect exception analysis.  An
 *  intervening finally that cannot complete normally allows us to ignore
 *  an otherwise uncaught exception.
 *
 *  <p>To implement the semantics of intervening finally clauses, all
 *  nonlocal transfers (break, continue, return, throw, method call that
 *  can throw a checked exception, and a constructor invocation that can
 *  thrown a checked exception) are recorded in a queue, and removed
 *  from the queue when we complete processing the target of the
 *  nonlocal transfer.  This allows us to modify the queue in accordance
 *  with the above rules when we encounter a finally clause.  The only
 *  exception to this [no pun intended] is that checked exceptions that
 *  are known to be caught or declared to be caught in the enclosing
 *  method are not recorded in the queue, but instead are recorded in a
 *  global variable "{@code Set<Type> thrown}" that records the type of all
 *  exceptions that can be thrown.
 *
 *  <p>Other minor issues the treatment of members of other classes
 *  (always considered DA except that within an anonymous class
 *  constructor, where DA status from the enclosing scope is
 *  preserved), treatment of the case expression (V is DA before the
 *  case expression iff V is DA after the switch expression),
 *  treatment of variables declared in a switch block (the implied
 *  DA/DU status after the switch expression is DU and not DA for
 *  variables defined in a switch block), the treatment of boolean ?:
 *  expressions (The JLS rules only handle b and c non-boolean; the
 *  new rule is that if b and c are boolean valued, then V is
 *  (un)assigned after a?b:c when true/false iff V is (un)assigned
 *  after b when true/false and V is (un)assigned after c when
 *  true/false).
 *
 *  <p>There is the remaining question of what syntactic forms constitute a
 *  reference to a variable.  It is conventional to allow this.x on the
 *  left-hand-side to initialize a final instance field named x, yet
 *  this.x isn't considered a "use" when appearing on a right-hand-side
 *  in most implementations.  Should parentheses affect what is
 *  considered a variable reference?  The simplest rule would be to
 *  allow unqualified forms only, parentheses optional, and phase out
 *  support for assigning to a final field via this.x.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class Flow {
    protected static final Context.Key<Flow> flowKey =
        new Context.Key<Flow>();

    private final Names names;
    private final Log log;
    private final Symtab syms;
    private final Types types;
    private final Check chk;
    private       TreeMaker make;
    private final Resolve rs;
    private final JCDiagnostic.Factory diags;
    private Env<AttrContext> attrEnv;
    private       Lint lint;
    private final boolean allowImprovedRethrowAnalysis;
    private final boolean allowImprovedCatchAnalysis;
    private final boolean allowEffectivelyFinalInInnerClasses;

    public static Flow instance(Context context) {
        Flow instance = context.get(flowKey);
        if (instance == null)
            instance = new Flow(context);
        return instance;
    }

    public void analyzeTree(Env<AttrContext> env, TreeMaker make) {
        new AliveAnalyzer().analyzeTree(env, make);
        new AssignAnalyzer().analyzeTree(env, make);
        new FlowAnalyzer().analyzeTree(env, make);
        new CaptureAnalyzer().analyzeTree(env, make);
    }

    public void analyzeLambda(Env<AttrContext> env, JCLambda that, TreeMaker make, boolean speculative) {
        Log.DiagnosticHandler diagHandler = null;
        //we need to disable diagnostics temporarily; the problem is that if
        //a lambda expression contains e.g. an unreachable statement, an error
        //message will be reported and will cause compilation to skip the flow analyis
        //step - if we suppress diagnostics, we won't stop at Attr for flow-analysis
        //related errors, which will allow for more errors to be detected
        if (!speculative) {
            diagHandler = new Log.DiscardDiagnosticHandler(log);
        }
        try {
            new AliveAnalyzer().analyzeTree(env, that, make);
            new FlowAnalyzer().analyzeTree(env, that, make);
        } finally {
            if (!speculative) {
                log.popDiagnosticHandler(diagHandler);
            }
        }
    }

    /**
     * Definite assignment scan mode
     */
    enum FlowKind {
        /**
         * This is the normal DA/DU analysis mode
         */
        NORMAL("var.might.already.be.assigned", false),
        /**
         * This is the speculative DA/DU analysis mode used to speculatively
         * derive assertions within loop bodies
         */
        SPECULATIVE_LOOP("var.might.be.assigned.in.loop", true);

        String errKey;
        boolean isFinal;

        FlowKind(String errKey, boolean isFinal) {
            this.errKey = errKey;
            this.isFinal = isFinal;
        }

        boolean isFinal() {
            return isFinal;
        }
    }

    protected Flow(Context context) {
        context.put(flowKey, this);
        names = Names.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        types = Types.instance(context);
        chk = Check.instance(context);
        lint = Lint.instance(context);
        rs = Resolve.instance(context);
        diags = JCDiagnostic.Factory.instance(context);
        Source source = Source.instance(context);
        allowImprovedRethrowAnalysis = source.allowImprovedRethrowAnalysis();
        allowImprovedCatchAnalysis = source.allowImprovedCatchAnalysis();
        Options options = Options.instance(context);
        allowEffectivelyFinalInInnerClasses = source.allowEffectivelyFinalInInnerClasses() &&
                options.isSet("allowEffectivelyFinalInInnerClasses"); //pre-lambda guard
    }

    /**
     * Base visitor class for all visitors implementing dataflow analysis logic.
     * This class define the shared logic for handling jumps (break/continue statements).
     */
    static abstract class BaseAnalyzer<P extends BaseAnalyzer.PendingExit> extends TreeScanner {

        enum JumpKind {
            BREAK(JCTree.Tag.BREAK) {
                @Override
                JCTree getTarget(JCTree tree) {
                    return ((JCBreak)tree).target;
                }
            },
            CONTINUE(JCTree.Tag.CONTINUE) {
                @Override
                JCTree getTarget(JCTree tree) {
                    return ((JCContinue)tree).target;
                }
            };

            JCTree.Tag treeTag;

            private JumpKind(Tag treeTag) {
                this.treeTag = treeTag;
            }

            abstract JCTree getTarget(JCTree tree);
        }

        /** The currently pending exits that go from current inner blocks
         *  to an enclosing block, in source order.
         */
        ListBuffer<P> pendingExits;

        /** A pending exit.  These are the statements return, break, and
         *  continue.  In addition, exception-throwing expressions or
         *  statements are put here when not known to be caught.  This
         *  will typically result in an error unless it is within a
         *  try-finally whose finally block cannot complete normally.
         */
        static class PendingExit {
            JCTree tree;

            PendingExit(JCTree tree) {
                this.tree = tree;
            }

            void resolveJump() {
                //do nothing
            }
        }

        abstract void markDead();

        /** Record an outward transfer of control. */
        void recordExit(JCTree tree, P pe) {
            pendingExits.append(pe);
            markDead();
        }

        /** Resolve all jumps of this statement. */
        private boolean resolveJump(JCTree tree,
                        ListBuffer<P> oldPendingExits,
                        JumpKind jk) {
            boolean resolved = false;
            List<P> exits = pendingExits.toList();
            pendingExits = oldPendingExits;
            for (; exits.nonEmpty(); exits = exits.tail) {
                P exit = exits.head;
                if (exit.tree.hasTag(jk.treeTag) &&
                        jk.getTarget(exit.tree) == tree) {
                    exit.resolveJump();
                    resolved = true;
                } else {
                    pendingExits.append(exit);
                }
            }
            return resolved;
        }

        /** Resolve all breaks of this statement. */
        boolean resolveContinues(JCTree tree) {
            return resolveJump(tree, new ListBuffer<P>(), JumpKind.CONTINUE);
        }

        /** Resolve all continues of this statement. */
        boolean resolveBreaks(JCTree tree, ListBuffer<P> oldPendingExits) {
            return resolveJump(tree, oldPendingExits, JumpKind.BREAK);
        }
    }

    /**
     * This pass implements the first step of the dataflow analysis, namely
     * the liveness analysis check. This checks that every statement is reachable.
     * The output of this analysis pass are used by other analyzers. This analyzer
     * sets the 'finallyCanCompleteNormally' field in the JCTry class.
     */
    class AliveAnalyzer extends BaseAnalyzer<BaseAnalyzer.PendingExit> {

        /** A flag that indicates whether the last statement could
         *  complete normally.
         */
        private boolean alive;

        @Override
        void markDead() {
            alive = false;
        }

    /*************************************************************************
     * Visitor methods for statements and definitions
     *************************************************************************/

        /** Analyze a definition.
         */
        void scanDef(JCTree tree) {
            scanStat(tree);
            if (tree != null && tree.hasTag(JCTree.Tag.BLOCK) && !alive) {
                log.error(tree.pos(),
                          "initializer.must.be.able.to.complete.normally");
            }
        }

        /** Analyze a statement. Check that statement is reachable.
         */
        void scanStat(JCTree tree) {
            if (!alive && tree != null) {
                log.error(tree.pos(), "unreachable.stmt");
                if (!tree.hasTag(SKIP)) alive = true;
            }
            scan(tree);
        }

        /** Analyze list of statements.
         */
        void scanStats(List<? extends JCStatement> trees) {
            if (trees != null)
                for (List<? extends JCStatement> l = trees; l.nonEmpty(); l = l.tail)
                    scanStat(l.head);
        }

        /* ------------ Visitor methods for various sorts of trees -------------*/

        public void visitClassDef(JCClassDecl tree) {
            if (tree.sym == null) return;
            boolean alivePrev = alive;
            ListBuffer<PendingExit> pendingExitsPrev = pendingExits;
            Lint lintPrev = lint;

            pendingExits = new ListBuffer<PendingExit>();
            lint = lint.augment(tree.sym.annotations);

            try {
                // process all the static initializers
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (!l.head.hasTag(METHODDEF) &&
                        (TreeInfo.flags(l.head) & STATIC) != 0) {
                        scanDef(l.head);
                    }
                }

                // process all the instance initializers
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (!l.head.hasTag(METHODDEF) &&
                        (TreeInfo.flags(l.head) & STATIC) == 0) {
                        scanDef(l.head);
                    }
                }

                // process all the methods
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (l.head.hasTag(METHODDEF)) {
                        scan(l.head);
                    }
                }
            } finally {
                pendingExits = pendingExitsPrev;
                alive = alivePrev;
                lint = lintPrev;
            }
        }

        public void visitMethodDef(JCMethodDecl tree) {
            if (tree.body == null) return;
            Lint lintPrev = lint;

            lint = lint.augment(tree.sym.annotations);

            Assert.check(pendingExits.isEmpty());

            try {
                alive = true;
                scanStat(tree.body);

                if (alive && !tree.sym.type.getReturnType().hasTag(VOID))
                    log.error(TreeInfo.diagEndPos(tree.body), "missing.ret.stmt");

                List<PendingExit> exits = pendingExits.toList();
                pendingExits = new ListBuffer<PendingExit>();
                while (exits.nonEmpty()) {
                    PendingExit exit = exits.head;
                    exits = exits.tail;
                    Assert.check(exit.tree.hasTag(RETURN));
                }
            } finally {
                lint = lintPrev;
            }
        }

        public void visitVarDef(JCVariableDecl tree) {
            if (tree.init != null) {
                Lint lintPrev = lint;
                lint = lint.augment(tree.sym.annotations);
                try{
                    scan(tree.init);
                } finally {
                    lint = lintPrev;
                }
            }
        }

        public void visitBlock(JCBlock tree) {
            scanStats(tree.stats);
        }

        public void visitDoLoop(JCDoWhileLoop tree) {
            ListBuffer<PendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<PendingExit>();
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            scan(tree.cond);
            alive = alive && !tree.cond.type.isTrue();
            alive |= resolveBreaks(tree, prevPendingExits);
        }

        public void visitWhileLoop(JCWhileLoop tree) {
            ListBuffer<PendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<PendingExit>();
            scan(tree.cond);
            alive = !tree.cond.type.isFalse();
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            alive = resolveBreaks(tree, prevPendingExits) ||
                !tree.cond.type.isTrue();
        }

        public void visitForLoop(JCForLoop tree) {
            ListBuffer<PendingExit> prevPendingExits = pendingExits;
            scanStats(tree.init);
            pendingExits = new ListBuffer<PendingExit>();
            if (tree.cond != null) {
                scan(tree.cond);
                alive = !tree.cond.type.isFalse();
            } else {
                alive = true;
            }
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            scan(tree.step);
            alive = resolveBreaks(tree, prevPendingExits) ||
                tree.cond != null && !tree.cond.type.isTrue();
        }

        public void visitForeachLoop(JCEnhancedForLoop tree) {
            visitVarDef(tree.var);
            ListBuffer<PendingExit> prevPendingExits = pendingExits;
            scan(tree.expr);
            pendingExits = new ListBuffer<PendingExit>();
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            resolveBreaks(tree, prevPendingExits);
            alive = true;
        }

        public void visitLabelled(JCLabeledStatement tree) {
            ListBuffer<PendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<PendingExit>();
            scanStat(tree.body);
            alive |= resolveBreaks(tree, prevPendingExits);
        }

        public void visitSwitch(JCSwitch tree) {
            ListBuffer<PendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<PendingExit>();
            scan(tree.selector);
            boolean hasDefault = false;
            for (List<JCCase> l = tree.cases; l.nonEmpty(); l = l.tail) {
                alive = true;
                JCCase c = l.head;
                if (c.pat == null)
                    hasDefault = true;
                else
                    scan(c.pat);
                scanStats(c.stats);
                // Warn about fall-through if lint switch fallthrough enabled.
                if (alive &&
                    lint.isEnabled(Lint.LintCategory.FALLTHROUGH) &&
                    c.stats.nonEmpty() && l.tail.nonEmpty())
                    log.warning(Lint.LintCategory.FALLTHROUGH,
                                l.tail.head.pos(),
                                "possible.fall-through.into.case");
            }
            if (!hasDefault) {
                alive = true;
            }
            alive |= resolveBreaks(tree, prevPendingExits);
        }

        public void visitTry(JCTry tree) {
            ListBuffer<PendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<PendingExit>();
            for (JCTree resource : tree.resources) {
                if (resource instanceof JCVariableDecl) {
                    JCVariableDecl vdecl = (JCVariableDecl) resource;
                    visitVarDef(vdecl);
                } else if (resource instanceof JCExpression) {
                    scan((JCExpression) resource);
                } else {
                    throw new AssertionError(tree);  // parser error
                }
            }

            scanStat(tree.body);
            boolean aliveEnd = alive;

            for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
                alive = true;
                JCVariableDecl param = l.head.param;
                scan(param);
                scanStat(l.head.body);
                aliveEnd |= alive;
            }
            if (tree.finalizer != null) {
                ListBuffer<PendingExit> exits = pendingExits;
                pendingExits = prevPendingExits;
                alive = true;
                scanStat(tree.finalizer);
                tree.finallyCanCompleteNormally = alive;
                if (!alive) {
                    if (lint.isEnabled(Lint.LintCategory.FINALLY)) {
                        log.warning(Lint.LintCategory.FINALLY,
                                TreeInfo.diagEndPos(tree.finalizer),
                                "finally.cannot.complete");
                    }
                } else {
                    while (exits.nonEmpty()) {
                        pendingExits.append(exits.next());
                    }
                    alive = aliveEnd;
                }
            } else {
                alive = aliveEnd;
                ListBuffer<PendingExit> exits = pendingExits;
                pendingExits = prevPendingExits;
                while (exits.nonEmpty()) pendingExits.append(exits.next());
            }
        }

        @Override
        public void visitIf(JCIf tree) {
            scan(tree.cond);
            scanStat(tree.thenpart);
            if (tree.elsepart != null) {
                boolean aliveAfterThen = alive;
                alive = true;
                scanStat(tree.elsepart);
                alive = alive | aliveAfterThen;
            } else {
                alive = true;
            }
        }

        public void visitBreak(JCBreak tree) {
            recordExit(tree, new PendingExit(tree));
        }

        public void visitContinue(JCContinue tree) {
            recordExit(tree, new PendingExit(tree));
        }

        public void visitReturn(JCReturn tree) {
            scan(tree.expr);
            recordExit(tree, new PendingExit(tree));
        }

        public void visitThrow(JCThrow tree) {
            scan(tree.expr);
            markDead();
        }

        public void visitApply(JCMethodInvocation tree) {
            scan(tree.meth);
            scan(tree.args);
        }

        public void visitNewClass(JCNewClass tree) {
            scan(tree.encl);
            scan(tree.args);
            if (tree.def != null) {
                scan(tree.def);
            }
        }

        @Override
        public void visitLambda(JCLambda tree) {
            if (tree.type != null &&
                    tree.type.isErroneous()) {
                return;
            }

            ListBuffer<PendingExit> prevPending = pendingExits;
            boolean prevAlive = alive;
            try {
                pendingExits = ListBuffer.lb();
                alive = true;
                scanStat(tree.body);
                tree.canCompleteNormally = alive;
            }
            finally {
                pendingExits = prevPending;
                alive = prevAlive;
            }
        }

        public void visitTopLevel(JCCompilationUnit tree) {
            // Do nothing for TopLevel since each class is visited individually
        }

    /**************************************************************************
     * main method
     *************************************************************************/

        /** Perform definite assignment/unassignment analysis on a tree.
         */
        public void analyzeTree(Env<AttrContext> env, TreeMaker make) {
            analyzeTree(env, env.tree, make);
        }
        public void analyzeTree(Env<AttrContext> env, JCTree tree, TreeMaker make) {
            try {
                attrEnv = env;
                Flow.this.make = make;
                pendingExits = new ListBuffer<PendingExit>();
                alive = true;
                scan(env.tree);
            } finally {
                pendingExits = null;
                Flow.this.make = null;
            }
        }
    }

    /**
     * This pass implements the second step of the dataflow analysis, namely
     * the exception analysis. This is to ensure that every checked exception that is
     * thrown is declared or caught. The analyzer uses some info that has been set by
     * the liveliness analyzer.
     */
    class FlowAnalyzer extends BaseAnalyzer<FlowAnalyzer.FlowPendingExit> {

        /** A flag that indicates whether the last statement could
         *  complete normally.
         */
        HashMap<Symbol, List<Type>> preciseRethrowTypes;

        /** The current class being defined.
         */
        JCClassDecl classDef;

        /** The list of possibly thrown declarable exceptions.
         */
        List<Type> thrown;

        /** The list of exceptions that are either caught or declared to be
         *  thrown.
         */
        List<Type> caught;

        class FlowPendingExit extends BaseAnalyzer.PendingExit {

            Type thrown;

            FlowPendingExit(JCTree tree, Type thrown) {
                super(tree);
                this.thrown = thrown;
            }
        }

        @Override
        void markDead() {
            //do nothing
        }

        /*-------------------- Exceptions ----------------------*/

        /** Complain that pending exceptions are not caught.
         */
        void errorUncaught() {
            for (FlowPendingExit exit = pendingExits.next();
                 exit != null;
                 exit = pendingExits.next()) {
                if (classDef != null &&
                    classDef.pos == exit.tree.pos) {
                    log.error(exit.tree.pos(),
                            "unreported.exception.default.constructor",
                            exit.thrown);
                } else if (exit.tree.hasTag(VARDEF) &&
                        ((JCVariableDecl)exit.tree).sym.isResourceVariable()) {
                    log.error(exit.tree.pos(),
                            "unreported.exception.implicit.close",
                            exit.thrown,
                            ((JCVariableDecl)exit.tree).sym.name);
                } else {
                    log.error(exit.tree.pos(),
                            "unreported.exception.need.to.catch.or.throw",
                            exit.thrown);
                }
            }
        }

        /** Record that exception is potentially thrown and check that it
         *  is caught.
         */
        void markThrown(JCTree tree, Type exc) {
            if (!chk.isUnchecked(tree.pos(), exc)) {
                if (!chk.isHandled(exc, caught))
                    pendingExits.append(new FlowPendingExit(tree, exc));
                    thrown = chk.incl(exc, thrown);
            }
        }

    /*************************************************************************
     * Visitor methods for statements and definitions
     *************************************************************************/

        /* ------------ Visitor methods for various sorts of trees -------------*/

        public void visitClassDef(JCClassDecl tree) {
            if (tree.sym == null) return;

            JCClassDecl classDefPrev = classDef;
            List<Type> thrownPrev = thrown;
            List<Type> caughtPrev = caught;
            ListBuffer<FlowPendingExit> pendingExitsPrev = pendingExits;
            Lint lintPrev = lint;

            pendingExits = new ListBuffer<FlowPendingExit>();
            if (tree.name != names.empty) {
                caught = List.nil();
            }
            classDef = tree;
            thrown = List.nil();
            lint = lint.augment(tree.sym.annotations);

            try {
                // process all the static initializers
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (!l.head.hasTag(METHODDEF) &&
                        (TreeInfo.flags(l.head) & STATIC) != 0) {
                        scan(l.head);
                        errorUncaught();
                    }
                }

                // add intersection of all thrown clauses of initial constructors
                // to set of caught exceptions, unless class is anonymous.
                if (tree.name != names.empty) {
                    boolean firstConstructor = true;
                    for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                        if (TreeInfo.isInitialConstructor(l.head)) {
                            List<Type> mthrown =
                                ((JCMethodDecl) l.head).sym.type.getThrownTypes();
                            if (firstConstructor) {
                                caught = mthrown;
                                firstConstructor = false;
                            } else {
                                caught = chk.intersect(mthrown, caught);
                            }
                        }
                    }
                }

                // process all the instance initializers
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (!l.head.hasTag(METHODDEF) &&
                        (TreeInfo.flags(l.head) & STATIC) == 0) {
                        scan(l.head);
                        errorUncaught();
                    }
                }

                // in an anonymous class, add the set of thrown exceptions to
                // the throws clause of the synthetic constructor and propagate
                // outwards.
                // Changing the throws clause on the fly is okay here because
                // the anonymous constructor can't be invoked anywhere else,
                // and its type hasn't been cached.
                if (tree.name == names.empty) {
                    for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                        if (TreeInfo.isInitialConstructor(l.head)) {
                            JCMethodDecl mdef = (JCMethodDecl)l.head;
                            mdef.thrown = make.Types(thrown);
                            mdef.sym.type = types.createMethodTypeWithThrown(mdef.sym.type, thrown);
                        }
                    }
                    thrownPrev = chk.union(thrown, thrownPrev);
                }

                // process all the methods
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (l.head.hasTag(METHODDEF)) {
                        scan(l.head);
                        errorUncaught();
                    }
                }

                thrown = thrownPrev;
            } finally {
                pendingExits = pendingExitsPrev;
                caught = caughtPrev;
                classDef = classDefPrev;
                lint = lintPrev;
            }
        }

        public void visitMethodDef(JCMethodDecl tree) {
            if (tree.body == null) return;

            List<Type> caughtPrev = caught;
            List<Type> mthrown = tree.sym.type.getThrownTypes();
            Lint lintPrev = lint;

            lint = lint.augment(tree.sym.annotations);

            Assert.check(pendingExits.isEmpty());

            try {
                for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                    JCVariableDecl def = l.head;
                    scan(def);
                }
                if (TreeInfo.isInitialConstructor(tree))
                    caught = chk.union(caught, mthrown);
                else if ((tree.sym.flags() & (BLOCK | STATIC)) != BLOCK)
                    caught = mthrown;
                // else we are in an instance initializer block;
                // leave caught unchanged.

                scan(tree.body);

                List<FlowPendingExit> exits = pendingExits.toList();
                pendingExits = new ListBuffer<FlowPendingExit>();
                while (exits.nonEmpty()) {
                    FlowPendingExit exit = exits.head;
                    exits = exits.tail;
                    if (exit.thrown == null) {
                        Assert.check(exit.tree.hasTag(RETURN));
                    } else {
                        // uncaught throws will be reported later
                        pendingExits.append(exit);
                    }
                }
            } finally {
                caught = caughtPrev;
                lint = lintPrev;
            }
        }

        public void visitVarDef(JCVariableDecl tree) {
            if (tree.init != null) {
                Lint lintPrev = lint;
                lint = lint.augment(tree.sym.annotations);
                try{
                    scan(tree.init);
                } finally {
                    lint = lintPrev;
                }
            }
        }

        public void visitBlock(JCBlock tree) {
            scan(tree.stats);
        }

        public void visitDoLoop(JCDoWhileLoop tree) {
            ListBuffer<FlowPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<FlowPendingExit>();
            scan(tree.body);
            resolveContinues(tree);
            scan(tree.cond);
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitWhileLoop(JCWhileLoop tree) {
            ListBuffer<FlowPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<FlowPendingExit>();
            scan(tree.cond);
            scan(tree.body);
            resolveContinues(tree);
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitForLoop(JCForLoop tree) {
            ListBuffer<FlowPendingExit> prevPendingExits = pendingExits;
            scan(tree.init);
            pendingExits = new ListBuffer<FlowPendingExit>();
            if (tree.cond != null) {
                scan(tree.cond);
            }
            scan(tree.body);
            resolveContinues(tree);
            scan(tree.step);
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitForeachLoop(JCEnhancedForLoop tree) {
            visitVarDef(tree.var);
            ListBuffer<FlowPendingExit> prevPendingExits = pendingExits;
            scan(tree.expr);
            pendingExits = new ListBuffer<FlowPendingExit>();
            scan(tree.body);
            resolveContinues(tree);
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitLabelled(JCLabeledStatement tree) {
            ListBuffer<FlowPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<FlowPendingExit>();
            scan(tree.body);
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitSwitch(JCSwitch tree) {
            ListBuffer<FlowPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<FlowPendingExit>();
            scan(tree.selector);
            for (List<JCCase> l = tree.cases; l.nonEmpty(); l = l.tail) {
                JCCase c = l.head;
                if (c.pat != null) {
                    scan(c.pat);
                }
                scan(c.stats);
            }
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitTry(JCTry tree) {
            List<Type> caughtPrev = caught;
            List<Type> thrownPrev = thrown;
            thrown = List.nil();
            for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
                List<JCExpression> subClauses = TreeInfo.isMultiCatch(l.head) ?
                        ((JCTypeUnion)l.head.param.vartype).alternatives :
                        List.of(l.head.param.vartype);
                for (JCExpression ct : subClauses) {
                    caught = chk.incl(ct.type, caught);
                }
            }

            ListBuffer<FlowPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<FlowPendingExit>();
            for (JCTree resource : tree.resources) {
                if (resource instanceof JCVariableDecl) {
                    JCVariableDecl vdecl = (JCVariableDecl) resource;
                    visitVarDef(vdecl);
                } else if (resource instanceof JCExpression) {
                    scan((JCExpression) resource);
                } else {
                    throw new AssertionError(tree);  // parser error
                }
            }
            for (JCTree resource : tree.resources) {
                List<Type> closeableSupertypes = resource.type.isCompound() ?
                    types.interfaces(resource.type).prepend(types.supertype(resource.type)) :
                    List.of(resource.type);
                for (Type sup : closeableSupertypes) {
                    if (types.asSuper(sup, syms.autoCloseableType.tsym) != null) {
                        Symbol closeMethod = rs.resolveQualifiedMethod(tree,
                                attrEnv,
                                sup,
                                names.close,
                                List.<Type>nil(),
                                List.<Type>nil());
                        if (closeMethod.kind == MTH) {
                            for (Type t : ((MethodSymbol)closeMethod).getThrownTypes()) {
                                markThrown(resource, t);
                            }
                        }
                    }
                }
            }
            scan(tree.body);
            List<Type> thrownInTry = allowImprovedCatchAnalysis ?
                chk.union(thrown, List.of(syms.runtimeExceptionType, syms.errorType)) :
                thrown;
            thrown = thrownPrev;
            caught = caughtPrev;

            List<Type> caughtInTry = List.nil();
            for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
                JCVariableDecl param = l.head.param;
                List<JCExpression> subClauses = TreeInfo.isMultiCatch(l.head) ?
                        ((JCTypeUnion)l.head.param.vartype).alternatives :
                        List.of(l.head.param.vartype);
                List<Type> ctypes = List.nil();
                List<Type> rethrownTypes = chk.diff(thrownInTry, caughtInTry);
                for (JCExpression ct : subClauses) {
                    Type exc = ct.type;
                    if (exc != syms.unknownType) {
                        ctypes = ctypes.append(exc);
                        if (types.isSameType(exc, syms.objectType))
                            continue;
                        checkCaughtType(l.head.pos(), exc, thrownInTry, caughtInTry);
                        caughtInTry = chk.incl(exc, caughtInTry);
                    }
                }
                scan(param);
                preciseRethrowTypes.put(param.sym, chk.intersect(ctypes, rethrownTypes));
                scan(l.head.body);
                preciseRethrowTypes.remove(param.sym);
            }
            if (tree.finalizer != null) {
                List<Type> savedThrown = thrown;
                thrown = List.nil();
                ListBuffer<FlowPendingExit> exits = pendingExits;
                pendingExits = prevPendingExits;
                scan(tree.finalizer);
                if (!tree.finallyCanCompleteNormally) {
                    // discard exits and exceptions from try and finally
                    thrown = chk.union(thrown, thrownPrev);
                } else {
                    thrown = chk.union(thrown, chk.diff(thrownInTry, caughtInTry));
                    thrown = chk.union(thrown, savedThrown);
                    // FIX: this doesn't preserve source order of exits in catch
                    // versus finally!
                    while (exits.nonEmpty()) {
                        pendingExits.append(exits.next());
                    }
                }
            } else {
                thrown = chk.union(thrown, chk.diff(thrownInTry, caughtInTry));
                ListBuffer<FlowPendingExit> exits = pendingExits;
                pendingExits = prevPendingExits;
                while (exits.nonEmpty()) pendingExits.append(exits.next());
            }
        }

        @Override
        public void visitIf(JCIf tree) {
            scan(tree.cond);
            scan(tree.thenpart);
            if (tree.elsepart != null) {
                scan(tree.elsepart);
            }
        }

        void checkCaughtType(DiagnosticPosition pos, Type exc, List<Type> thrownInTry, List<Type> caughtInTry) {
            if (chk.subset(exc, caughtInTry)) {
                log.error(pos, "except.already.caught", exc);
            } else if (!chk.isUnchecked(pos, exc) &&
                    !isExceptionOrThrowable(exc) &&
                    !chk.intersects(exc, thrownInTry)) {
                log.error(pos, "except.never.thrown.in.try", exc);
            } else if (allowImprovedCatchAnalysis) {
                List<Type> catchableThrownTypes = chk.intersect(List.of(exc), thrownInTry);
                // 'catchableThrownTypes' cannnot possibly be empty - if 'exc' was an
                // unchecked exception, the result list would not be empty, as the augmented
                // thrown set includes { RuntimeException, Error }; if 'exc' was a checked
                // exception, that would have been covered in the branch above
                if (chk.diff(catchableThrownTypes, caughtInTry).isEmpty() &&
                        !isExceptionOrThrowable(exc)) {
                    String key = catchableThrownTypes.length() == 1 ?
                            "unreachable.catch" :
                            "unreachable.catch.1";
                    log.warning(pos, key, catchableThrownTypes);
                }
            }
        }
        //where
            private boolean isExceptionOrThrowable(Type exc) {
                return exc.tsym == syms.throwableType.tsym ||
                    exc.tsym == syms.exceptionType.tsym;
            }

        public void visitBreak(JCBreak tree) {
            recordExit(tree, new FlowPendingExit(tree, null));
        }

        public void visitContinue(JCContinue tree) {
            recordExit(tree, new FlowPendingExit(tree, null));
        }

        public void visitReturn(JCReturn tree) {
            scan(tree.expr);
            recordExit(tree, new FlowPendingExit(tree, null));
        }

        public void visitThrow(JCThrow tree) {
            scan(tree.expr);
            Symbol sym = TreeInfo.symbol(tree.expr);
            if (sym != null &&
                sym.kind == VAR &&
                (sym.flags() & (FINAL | EFFECTIVELY_FINAL)) != 0 &&
                preciseRethrowTypes.get(sym) != null &&
                allowImprovedRethrowAnalysis) {
                for (Type t : preciseRethrowTypes.get(sym)) {
                    markThrown(tree, t);
                }
            }
            else {
                markThrown(tree, tree.expr.type);
            }
            markDead();
        }

        public void visitApply(JCMethodInvocation tree) {
            scan(tree.meth);
            scan(tree.args);
            for (List<Type> l = tree.meth.type.getThrownTypes(); l.nonEmpty(); l = l.tail)
                markThrown(tree, l.head);
        }

        public void visitNewClass(JCNewClass tree) {
            scan(tree.encl);
            scan(tree.args);
           // scan(tree.def);
            for (List<Type> l = tree.constructorType.getThrownTypes();
                 l.nonEmpty();
                 l = l.tail) {
                markThrown(tree, l.head);
            }
            List<Type> caughtPrev = caught;
            try {
                // If the new class expression defines an anonymous class,
                // analysis of the anonymous constructor may encounter thrown
                // types which are unsubstituted type variables.
                // However, since the constructor's actual thrown types have
                // already been marked as thrown, it is safe to simply include
                // each of the constructor's formal thrown types in the set of
                // 'caught/declared to be thrown' types, for the duration of
                // the class def analysis.
                if (tree.def != null)
                    for (List<Type> l = tree.constructor.type.getThrownTypes();
                         l.nonEmpty();
                         l = l.tail) {
                        caught = chk.incl(l.head, caught);
                    }
                scan(tree.def);
            }
            finally {
                caught = caughtPrev;
            }
        }

        @Override
        public void visitLambda(JCLambda tree) {
            if (tree.type != null &&
                    tree.type.isErroneous()) {
                return;
            }
            List<Type> prevCaught = caught;
            List<Type> prevThrown = thrown;
            ListBuffer<FlowPendingExit> prevPending = pendingExits;
            try {
                pendingExits = ListBuffer.lb();
                caught = List.of(syms.throwableType); //inhibit exception checking
                thrown = List.nil();
                scan(tree.body);
                tree.inferredThrownTypes = thrown;
            }
            finally {
                pendingExits = prevPending;
                caught = prevCaught;
                thrown = prevThrown;
            }
        }

        public void visitTopLevel(JCCompilationUnit tree) {
            // Do nothing for TopLevel since each class is visited individually
        }

    /**************************************************************************
     * main method
     *************************************************************************/

        /** Perform definite assignment/unassignment analysis on a tree.
         */
        public void analyzeTree(Env<AttrContext> env, TreeMaker make) {
            analyzeTree(env, env.tree, make);
        }
        public void analyzeTree(Env<AttrContext> env, JCTree tree, TreeMaker make) {
            try {
                attrEnv = env;
                Flow.this.make = make;
                pendingExits = new ListBuffer<FlowPendingExit>();
                preciseRethrowTypes = new HashMap<Symbol, List<Type>>();
                this.thrown = this.caught = null;
                this.classDef = null;
                scan(tree);
            } finally {
                pendingExits = null;
                Flow.this.make = null;
                this.thrown = this.caught = null;
                this.classDef = null;
            }
        }
    }

    /**
     * This pass implements (i) definite assignment analysis, which ensures that
     * each variable is assigned when used and (ii) definite unassignment analysis,
     * which ensures that no final variable is assigned more than once. This visitor
     * depends on the results of the liveliness analyzer. This pass is also used to mark
     * effectively-final local variables/parameters.
     */
    class AssignAnalyzer extends BaseAnalyzer<AssignAnalyzer.AssignPendingExit> {

        /** The set of definitely assigned variables.
         */
        Bits inits;

        /** The set of definitely unassigned variables.
         */
        Bits uninits;

        /** The set of variables that are definitely unassigned everywhere
         *  in current try block. This variable is maintained lazily; it is
         *  updated only when something gets removed from uninits,
         *  typically by being assigned in reachable code.  To obtain the
         *  correct set of variables which are definitely unassigned
         *  anywhere in current try block, intersect uninitsTry and
         *  uninits.
         */
        Bits uninitsTry;

        /** When analyzing a condition, inits and uninits are null.
         *  Instead we have:
         */
        Bits initsWhenTrue;
        Bits initsWhenFalse;
        Bits uninitsWhenTrue;
        Bits uninitsWhenFalse;

        /** A mapping from addresses to variable symbols.
         */
        VarSymbol[] vars;

        /** The current class being defined.
         */
        JCClassDecl classDef;

        /** The first variable sequence number in this class definition.
         */
        int firstadr;

        /** The next available variable sequence number.
         */
        int nextadr;

        /** The first variable sequence number in a block that can return.
         */
        int returnadr;

        /** The list of unreferenced automatic resources.
         */
        Scope unrefdResources;

        /** Set when processing a loop body the second time for DU analysis. */
        FlowKind flowKind = FlowKind.NORMAL;

        /** The starting position of the analysed tree */
        int startPos;

        class AssignPendingExit extends BaseAnalyzer.PendingExit {

            Bits exit_inits;
            Bits exit_uninits;

            AssignPendingExit(JCTree tree, Bits inits, Bits uninits) {
                super(tree);
                this.exit_inits = inits.dup();
                this.exit_uninits = uninits.dup();
            }

            void resolveJump() {
                inits.andSet(exit_inits);
                uninits.andSet(exit_uninits);
            }
        }

        @Override
        void markDead() {
            inits.inclRange(returnadr, nextadr);
            uninits.inclRange(returnadr, nextadr);
        }

        /*-------------- Processing variables ----------------------*/

        /** Do we need to track init/uninit state of this symbol?
         *  I.e. is symbol either a local or a blank final variable?
         */
        boolean trackable(VarSymbol sym) {
            return
                sym.pos >= startPos &&
                ((sym.owner.kind == MTH ||
                 ((sym.flags() & (FINAL | HASINIT | PARAMETER)) == FINAL &&
                  classDef.sym.isEnclosedBy((ClassSymbol)sym.owner))));
        }

        /** Initialize new trackable variable by setting its address field
         *  to the next available sequence number and entering it under that
         *  index into the vars array.
         */
        void newVar(VarSymbol sym) {
            vars = ArrayUtils.ensureCapacity(vars, nextadr);
            if ((sym.flags() & FINAL) == 0) {
                sym.flags_field |= EFFECTIVELY_FINAL;
            }
            sym.adr = nextadr;
            vars[nextadr] = sym;
            inits.excl(nextadr);
            uninits.incl(nextadr);
            nextadr++;
        }

        /** Record an initialization of a trackable variable.
         */
        void letInit(DiagnosticPosition pos, VarSymbol sym) {
            if (sym.adr >= firstadr && trackable(sym)) {
                if ((sym.flags() & EFFECTIVELY_FINAL) != 0) {
                    if (!uninits.isMember(sym.adr)) {
                        //assignment targeting an effectively final variable
                        //makes the variable lose its status of effectively final
                        //if the variable is _not_ definitively unassigned
                        sym.flags_field &= ~EFFECTIVELY_FINAL;
                    } else {
                        uninit(sym);
                    }
                }
                else if ((sym.flags() & FINAL) != 0) {
                    if ((sym.flags() & PARAMETER) != 0) {
                        if ((sym.flags() & UNION) != 0) { //multi-catch parameter
                            log.error(pos, "multicatch.parameter.may.not.be.assigned",
                                      sym);
                        }
                        else {
                            log.error(pos, "final.parameter.may.not.be.assigned",
                                  sym);
                        }
                    } else if (!uninits.isMember(sym.adr)) {
                        log.error(pos, flowKind.errKey, sym);
                    } else {
                        uninit(sym);
                    }
                }
                inits.incl(sym.adr);
            } else if ((sym.flags() & FINAL) != 0) {
                log.error(pos, "var.might.already.be.assigned", sym);
            }
        }
        //where
            void uninit(VarSymbol sym) {
                if (!inits.isMember(sym.adr)) {
                    // reachable assignment
                    uninits.excl(sym.adr);
                    uninitsTry.excl(sym.adr);
                } else {
                    //log.rawWarning(pos, "unreachable assignment");//DEBUG
                    uninits.excl(sym.adr);
                }
            }

        /** If tree is either a simple name or of the form this.name or
         *  C.this.name, and tree represents a trackable variable,
         *  record an initialization of the variable.
         */
        void letInit(JCTree tree) {
            tree = TreeInfo.skipParens(tree);
            if (tree.hasTag(IDENT) || tree.hasTag(SELECT)) {
                Symbol sym = TreeInfo.symbol(tree);
                if (sym.kind == VAR) {
                    letInit(tree.pos(), (VarSymbol)sym);
                }
            }
        }

        /** Check that trackable variable is initialized.
         */
        void checkInit(DiagnosticPosition pos, VarSymbol sym) {
            if ((sym.adr >= firstadr || sym.owner.kind != TYP) &&
                trackable(sym) &&
                !inits.isMember(sym.adr)) {
                log.error(pos, "var.might.not.have.been.initialized",
                          sym);
                inits.incl(sym.adr);
            }
        }

        /** Split (duplicate) inits/uninits into WhenTrue/WhenFalse sets
         */
        void split(boolean setToNull) {
            initsWhenFalse = inits.dup();
            uninitsWhenFalse = uninits.dup();
            initsWhenTrue = inits;
            uninitsWhenTrue = uninits;
            if (setToNull)
                inits = uninits = null;
        }

        /** Merge (intersect) inits/uninits from WhenTrue/WhenFalse sets.
         */
        void merge() {
            inits = initsWhenFalse.andSet(initsWhenTrue);
            uninits = uninitsWhenFalse.andSet(uninitsWhenTrue);
        }

    /* ************************************************************************
     * Visitor methods for statements and definitions
     *************************************************************************/

        /** Analyze an expression. Make sure to set (un)inits rather than
         *  (un)initsWhenTrue(WhenFalse) on exit.
         */
        void scanExpr(JCTree tree) {
            if (tree != null) {
                scan(tree);
                if (inits == null) merge();
            }
        }

        /** Analyze a list of expressions.
         */
        void scanExprs(List<? extends JCExpression> trees) {
            if (trees != null)
                for (List<? extends JCExpression> l = trees; l.nonEmpty(); l = l.tail)
                    scanExpr(l.head);
        }

        /** Analyze a condition. Make sure to set (un)initsWhenTrue(WhenFalse)
         *  rather than (un)inits on exit.
         */
        void scanCond(JCTree tree) {
            if (tree.type.isFalse()) {
                if (inits == null) merge();
                initsWhenTrue = inits.dup();
                initsWhenTrue.inclRange(firstadr, nextadr);
                uninitsWhenTrue = uninits.dup();
                uninitsWhenTrue.inclRange(firstadr, nextadr);
                initsWhenFalse = inits;
                uninitsWhenFalse = uninits;
            } else if (tree.type.isTrue()) {
                if (inits == null) merge();
                initsWhenFalse = inits.dup();
                initsWhenFalse.inclRange(firstadr, nextadr);
                uninitsWhenFalse = uninits.dup();
                uninitsWhenFalse.inclRange(firstadr, nextadr);
                initsWhenTrue = inits;
                uninitsWhenTrue = uninits;
            } else {
                scan(tree);
                if (inits != null)
                    split(tree.type != syms.unknownType);
            }
            if (tree.type != syms.unknownType)
                inits = uninits = null;
        }

        /* ------------ Visitor methods for various sorts of trees -------------*/

        public void visitClassDef(JCClassDecl tree) {
            if (tree.sym == null) return;

            JCClassDecl classDefPrev = classDef;
            int firstadrPrev = firstadr;
            int nextadrPrev = nextadr;
            ListBuffer<AssignPendingExit> pendingExitsPrev = pendingExits;
            Lint lintPrev = lint;

            pendingExits = new ListBuffer<AssignPendingExit>();
            if (tree.name != names.empty) {
                firstadr = nextadr;
            }
            classDef = tree;
            lint = lint.augment(tree.sym.annotations);

            try {
                // define all the static fields
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (l.head.hasTag(VARDEF)) {
                        JCVariableDecl def = (JCVariableDecl)l.head;
                        if ((def.mods.flags & STATIC) != 0) {
                            VarSymbol sym = def.sym;
                            if (trackable(sym))
                                newVar(sym);
                        }
                    }
                }

                // process all the static initializers
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (!l.head.hasTag(METHODDEF) &&
                        (TreeInfo.flags(l.head) & STATIC) != 0) {
                        scan(l.head);
                    }
                }

                // define all the instance fields
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (l.head.hasTag(VARDEF)) {
                        JCVariableDecl def = (JCVariableDecl)l.head;
                        if ((def.mods.flags & STATIC) == 0) {
                            VarSymbol sym = def.sym;
                            if (trackable(sym))
                                newVar(sym);
                        }
                    }
                }

                // process all the instance initializers
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (!l.head.hasTag(METHODDEF) &&
                        (TreeInfo.flags(l.head) & STATIC) == 0) {
                        scan(l.head);
                    }
                }

                // process all the methods
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (l.head.hasTag(METHODDEF)) {
                        scan(l.head);
                    }
                }
            } finally {
                pendingExits = pendingExitsPrev;
                nextadr = nextadrPrev;
                firstadr = firstadrPrev;
                classDef = classDefPrev;
                lint = lintPrev;
            }
        }

        public void visitMethodDef(JCMethodDecl tree) {
            if (tree.body == null) return;

            Bits initsPrev = inits.dup();
            Bits uninitsPrev = uninits.dup();
            int nextadrPrev = nextadr;
            int firstadrPrev = firstadr;
            int returnadrPrev = returnadr;
            Lint lintPrev = lint;

            lint = lint.augment(tree.sym.annotations);

            Assert.check(pendingExits.isEmpty());

            try {
                boolean isInitialConstructor =
                    TreeInfo.isInitialConstructor(tree);

                if (!isInitialConstructor)
                    firstadr = nextadr;
                for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                    JCVariableDecl def = l.head;
                    scan(def);
                    inits.incl(def.sym.adr);
                    uninits.excl(def.sym.adr);
                }
                // else we are in an instance initializer block;
                // leave caught unchanged.
                scan(tree.body);

                if (isInitialConstructor) {
                    for (int i = firstadr; i < nextadr; i++)
                        if (vars[i].owner == classDef.sym)
                            checkInit(TreeInfo.diagEndPos(tree.body), vars[i]);
                }
                List<AssignPendingExit> exits = pendingExits.toList();
                pendingExits = new ListBuffer<AssignPendingExit>();
                while (exits.nonEmpty()) {
                    AssignPendingExit exit = exits.head;
                    exits = exits.tail;
                    Assert.check(exit.tree.hasTag(RETURN), exit.tree);
                    if (isInitialConstructor) {
                        inits = exit.exit_inits;
                        for (int i = firstadr; i < nextadr; i++)
                            checkInit(exit.tree.pos(), vars[i]);
                    }
                }
            } finally {
                inits = initsPrev;
                uninits = uninitsPrev;
                nextadr = nextadrPrev;
                firstadr = firstadrPrev;
                returnadr = returnadrPrev;
                lint = lintPrev;
            }
        }

        public void visitVarDef(JCVariableDecl tree) {
            boolean track = trackable(tree.sym);
            if (track && tree.sym.owner.kind == MTH) newVar(tree.sym);
            if (tree.init != null) {
                Lint lintPrev = lint;
                lint = lint.augment(tree.sym.annotations);
                try{
                    scanExpr(tree.init);
                    if (track) letInit(tree.pos(), tree.sym);
                } finally {
                    lint = lintPrev;
                }
            }
        }

        public void visitBlock(JCBlock tree) {
            int nextadrPrev = nextadr;
            scan(tree.stats);
            nextadr = nextadrPrev;
        }

        public void visitDoLoop(JCDoWhileLoop tree) {
            ListBuffer<AssignPendingExit> prevPendingExits = pendingExits;
            FlowKind prevFlowKind = flowKind;
            flowKind = FlowKind.NORMAL;
            Bits initsSkip = null;
            Bits uninitsSkip = null;
            pendingExits = new ListBuffer<AssignPendingExit>();
            int prevErrors = log.nerrors;
            do {
                Bits uninitsEntry = uninits.dup();
                uninitsEntry.excludeFrom(nextadr);
                scan(tree.body);
                resolveContinues(tree);
                scanCond(tree.cond);
                if (!flowKind.isFinal()) {
                    initsSkip = initsWhenFalse;
                    uninitsSkip = uninitsWhenFalse;
                }
                if (log.nerrors !=  prevErrors ||
                    flowKind.isFinal() ||
                    uninitsEntry.dup().diffSet(uninitsWhenTrue).nextBit(firstadr)==-1)
                    break;
                inits = initsWhenTrue;
                uninits = uninitsEntry.andSet(uninitsWhenTrue);
                flowKind = FlowKind.SPECULATIVE_LOOP;
            } while (true);
            flowKind = prevFlowKind;
            inits = initsSkip;
            uninits = uninitsSkip;
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitWhileLoop(JCWhileLoop tree) {
            ListBuffer<AssignPendingExit> prevPendingExits = pendingExits;
            FlowKind prevFlowKind = flowKind;
            flowKind = FlowKind.NORMAL;
            Bits initsSkip = null;
            Bits uninitsSkip = null;
            pendingExits = new ListBuffer<AssignPendingExit>();
            int prevErrors = log.nerrors;
            Bits uninitsEntry = uninits.dup();
            uninitsEntry.excludeFrom(nextadr);
            do {
                scanCond(tree.cond);
                if (!flowKind.isFinal()) {
                    initsSkip = initsWhenFalse;
                    uninitsSkip = uninitsWhenFalse;
                }
                inits = initsWhenTrue;
                uninits = uninitsWhenTrue;
                scan(tree.body);
                resolveContinues(tree);
                if (log.nerrors != prevErrors ||
                    flowKind.isFinal() ||
                    uninitsEntry.dup().diffSet(uninits).nextBit(firstadr) == -1)
                    break;
                uninits = uninitsEntry.andSet(uninits);
                flowKind = FlowKind.SPECULATIVE_LOOP;
            } while (true);
            flowKind = prevFlowKind;
            //a variable is DA/DU after the while statement, if it's DA/DU assuming the
            //branch is not taken AND if it's DA/DU before any break statement
            inits = initsSkip;
            uninits = uninitsSkip;
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitForLoop(JCForLoop tree) {
            ListBuffer<AssignPendingExit> prevPendingExits = pendingExits;
            FlowKind prevFlowKind = flowKind;
            flowKind = FlowKind.NORMAL;
            int nextadrPrev = nextadr;
            scan(tree.init);
            Bits initsSkip = null;
            Bits uninitsSkip = null;
            pendingExits = new ListBuffer<AssignPendingExit>();
            int prevErrors = log.nerrors;
            do {
                Bits uninitsEntry = uninits.dup();
                uninitsEntry.excludeFrom(nextadr);
                if (tree.cond != null) {
                    scanCond(tree.cond);
                    if (!flowKind.isFinal()) {
                        initsSkip = initsWhenFalse;
                        uninitsSkip = uninitsWhenFalse;
                    }
                    inits = initsWhenTrue;
                    uninits = uninitsWhenTrue;
                } else if (!flowKind.isFinal()) {
                    initsSkip = inits.dup();
                    initsSkip.inclRange(firstadr, nextadr);
                    uninitsSkip = uninits.dup();
                    uninitsSkip.inclRange(firstadr, nextadr);
                }
                scan(tree.body);
                resolveContinues(tree);
                scan(tree.step);
                if (log.nerrors != prevErrors ||
                    flowKind.isFinal() ||
                    uninitsEntry.dup().diffSet(uninits).nextBit(firstadr) == -1)
                    break;
                uninits = uninitsEntry.andSet(uninits);
                flowKind = FlowKind.SPECULATIVE_LOOP;
            } while (true);
            flowKind = prevFlowKind;
            //a variable is DA/DU after a for loop, if it's DA/DU assuming the
            //branch is not taken AND if it's DA/DU before any break statement
            inits = initsSkip;
            uninits = uninitsSkip;
            resolveBreaks(tree, prevPendingExits);
            nextadr = nextadrPrev;
        }

        public void visitForeachLoop(JCEnhancedForLoop tree) {
            visitVarDef(tree.var);

            ListBuffer<AssignPendingExit> prevPendingExits = pendingExits;
            FlowKind prevFlowKind = flowKind;
            flowKind = FlowKind.NORMAL;
            int nextadrPrev = nextadr;
            scan(tree.expr);
            Bits initsStart = inits.dup();
            Bits uninitsStart = uninits.dup();

            letInit(tree.pos(), tree.var.sym);
            pendingExits = new ListBuffer<AssignPendingExit>();
            int prevErrors = log.nerrors;
            do {
                Bits uninitsEntry = uninits.dup();
                uninitsEntry.excludeFrom(nextadr);
                scan(tree.body);
                resolveContinues(tree);
                if (log.nerrors != prevErrors ||
                    flowKind.isFinal() ||
                    uninitsEntry.dup().diffSet(uninits).nextBit(firstadr) == -1)
                    break;
                uninits = uninitsEntry.andSet(uninits);
                flowKind = FlowKind.SPECULATIVE_LOOP;
            } while (true);
            flowKind = prevFlowKind;
            inits = initsStart;
            uninits = uninitsStart.andSet(uninits);
            resolveBreaks(tree, prevPendingExits);
            nextadr = nextadrPrev;
        }

        public void visitLabelled(JCLabeledStatement tree) {
            ListBuffer<AssignPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<AssignPendingExit>();
            scan(tree.body);
            resolveBreaks(tree, prevPendingExits);
        }

        public void visitSwitch(JCSwitch tree) {
            ListBuffer<AssignPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<AssignPendingExit>();
            int nextadrPrev = nextadr;
            scanExpr(tree.selector);
            Bits initsSwitch = inits;
            Bits uninitsSwitch = uninits.dup();
            boolean hasDefault = false;
            for (List<JCCase> l = tree.cases; l.nonEmpty(); l = l.tail) {
                inits = initsSwitch.dup();
                uninits = uninits.andSet(uninitsSwitch);
                JCCase c = l.head;
                if (c.pat == null)
                    hasDefault = true;
                else
                    scanExpr(c.pat);
                scan(c.stats);
                addVars(c.stats, initsSwitch, uninitsSwitch);
                // Warn about fall-through if lint switch fallthrough enabled.
            }
            if (!hasDefault) {
                inits.andSet(initsSwitch);
            }
            resolveBreaks(tree, prevPendingExits);
            nextadr = nextadrPrev;
        }
        // where
            /** Add any variables defined in stats to inits and uninits. */
            private void addVars(List<JCStatement> stats, Bits inits,
                                        Bits uninits) {
                for (;stats.nonEmpty(); stats = stats.tail) {
                    JCTree stat = stats.head;
                    if (stat.hasTag(VARDEF)) {
                        int adr = ((JCVariableDecl) stat).sym.adr;
                        inits.excl(adr);
                        uninits.incl(adr);
                    }
                }
            }

        public void visitTry(JCTry tree) {
            ListBuffer<JCVariableDecl> resourceVarDecls = ListBuffer.lb();
            Bits uninitsTryPrev = uninitsTry;
            ListBuffer<AssignPendingExit> prevPendingExits = pendingExits;
            pendingExits = new ListBuffer<AssignPendingExit>();
            Bits initsTry = inits.dup();
            uninitsTry = uninits.dup();
            for (JCTree resource : tree.resources) {
                if (resource instanceof JCVariableDecl) {
                    JCVariableDecl vdecl = (JCVariableDecl) resource;
                    visitVarDef(vdecl);
                    unrefdResources.enter(vdecl.sym);
                    resourceVarDecls.append(vdecl);
                } else if (resource instanceof JCExpression) {
                    scanExpr((JCExpression) resource);
                } else {
                    throw new AssertionError(tree);  // parser error
                }
            }
            scan(tree.body);
            uninitsTry.andSet(uninits);
            Bits initsEnd = inits;
            Bits uninitsEnd = uninits;
            int nextadrCatch = nextadr;

            if (!resourceVarDecls.isEmpty() &&
                    lint.isEnabled(Lint.LintCategory.TRY)) {
                for (JCVariableDecl resVar : resourceVarDecls) {
                    if (unrefdResources.includes(resVar.sym)) {
                        log.warning(Lint.LintCategory.TRY, resVar.pos(),
                                    "try.resource.not.referenced", resVar.sym);
                        unrefdResources.remove(resVar.sym);
                    }
                }
            }

            for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
                JCVariableDecl param = l.head.param;
                inits = initsTry.dup();
                uninits = uninitsTry.dup();
                scan(param);
                inits.incl(param.sym.adr);
                uninits.excl(param.sym.adr);
                scan(l.head.body);
                initsEnd.andSet(inits);
                uninitsEnd.andSet(uninits);
                nextadr = nextadrCatch;
            }
            if (tree.finalizer != null) {
                inits = initsTry.dup();
                uninits = uninitsTry.dup();
                ListBuffer<AssignPendingExit> exits = pendingExits;
                pendingExits = prevPendingExits;
                scan(tree.finalizer);
                if (!tree.finallyCanCompleteNormally) {
                    // discard exits and exceptions from try and finally
                } else {
                    uninits.andSet(uninitsEnd);
                    // FIX: this doesn't preserve source order of exits in catch
                    // versus finally!
                    while (exits.nonEmpty()) {
                        AssignPendingExit exit = exits.next();
                        if (exit.exit_inits != null) {
                            exit.exit_inits.orSet(inits);
                            exit.exit_uninits.andSet(uninits);
                        }
                        pendingExits.append(exit);
                    }
                    inits.orSet(initsEnd);
                }
            } else {
                inits = initsEnd;
                uninits = uninitsEnd;
                ListBuffer<AssignPendingExit> exits = pendingExits;
                pendingExits = prevPendingExits;
                while (exits.nonEmpty()) pendingExits.append(exits.next());
            }
            uninitsTry.andSet(uninitsTryPrev).andSet(uninits);
        }

        public void visitConditional(JCConditional tree) {
            scanCond(tree.cond);
            Bits initsBeforeElse = initsWhenFalse;
            Bits uninitsBeforeElse = uninitsWhenFalse;
            inits = initsWhenTrue;
            uninits = uninitsWhenTrue;
            if (tree.truepart.type.hasTag(BOOLEAN) &&
                tree.falsepart.type.hasTag(BOOLEAN)) {
                // if b and c are boolean valued, then
                // v is (un)assigned after a?b:c when true iff
                //    v is (un)assigned after b when true and
                //    v is (un)assigned after c when true
                scanCond(tree.truepart);
                Bits initsAfterThenWhenTrue = initsWhenTrue.dup();
                Bits initsAfterThenWhenFalse = initsWhenFalse.dup();
                Bits uninitsAfterThenWhenTrue = uninitsWhenTrue.dup();
                Bits uninitsAfterThenWhenFalse = uninitsWhenFalse.dup();
                inits = initsBeforeElse;
                uninits = uninitsBeforeElse;
                scanCond(tree.falsepart);
                initsWhenTrue.andSet(initsAfterThenWhenTrue);
                initsWhenFalse.andSet(initsAfterThenWhenFalse);
                uninitsWhenTrue.andSet(uninitsAfterThenWhenTrue);
                uninitsWhenFalse.andSet(uninitsAfterThenWhenFalse);
            } else {
                scanExpr(tree.truepart);
                Bits initsAfterThen = inits.dup();
                Bits uninitsAfterThen = uninits.dup();
                inits = initsBeforeElse;
                uninits = uninitsBeforeElse;
                scanExpr(tree.falsepart);
                inits.andSet(initsAfterThen);
                uninits.andSet(uninitsAfterThen);
            }
        }

        public void visitIf(JCIf tree) {
            scanCond(tree.cond);
            Bits initsBeforeElse = initsWhenFalse;
            Bits uninitsBeforeElse = uninitsWhenFalse;
            inits = initsWhenTrue;
            uninits = uninitsWhenTrue;
            scan(tree.thenpart);
            if (tree.elsepart != null) {
                Bits initsAfterThen = inits.dup();
                Bits uninitsAfterThen = uninits.dup();
                inits = initsBeforeElse;
                uninits = uninitsBeforeElse;
                scan(tree.elsepart);
                inits.andSet(initsAfterThen);
                uninits.andSet(uninitsAfterThen);
            } else {
                inits.andSet(initsBeforeElse);
                uninits.andSet(uninitsBeforeElse);
            }
        }

        public void visitBreak(JCBreak tree) {
            recordExit(tree, new AssignPendingExit(tree, inits, uninits));
        }

        public void visitContinue(JCContinue tree) {
            recordExit(tree, new AssignPendingExit(tree, inits, uninits));
        }

        public void visitReturn(JCReturn tree) {
            scanExpr(tree.expr);
            recordExit(tree, new AssignPendingExit(tree, inits, uninits));
        }

        public void visitThrow(JCThrow tree) {
            scanExpr(tree.expr);
            markDead();
        }

        public void visitApply(JCMethodInvocation tree) {
            scanExpr(tree.meth);
            scanExprs(tree.args);
        }

        public void visitNewClass(JCNewClass tree) {
            scanExpr(tree.encl);
            scanExprs(tree.args);
            scan(tree.def);
        }

        @Override
        public void visitLambda(JCLambda tree) {
            Bits prevUninits = uninits;
            Bits prevInits = inits;
            int returnadrPrev = returnadr;
            ListBuffer<AssignPendingExit> prevPending = pendingExits;
            try {
                returnadr = nextadr;
                pendingExits = new ListBuffer<AssignPendingExit>();
                for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                    JCVariableDecl def = l.head;
                    scan(def);
                    inits.incl(def.sym.adr);
                    uninits.excl(def.sym.adr);
                }
                if (tree.getBodyKind() == JCLambda.BodyKind.EXPRESSION) {
                    scanExpr(tree.body);
                } else {
                    scan(tree.body);
                }
            }
            finally {
                returnadr = returnadrPrev;
                uninits = prevUninits;
                inits = prevInits;
                pendingExits = prevPending;
            }
        }

        public void visitNewArray(JCNewArray tree) {
            scanExprs(tree.dims);
            scanExprs(tree.elems);
        }

        public void visitAssert(JCAssert tree) {
            Bits initsExit = inits.dup();
            Bits uninitsExit = uninits.dup();
            scanCond(tree.cond);
            uninitsExit.andSet(uninitsWhenTrue);
            if (tree.detail != null) {
                inits = initsWhenFalse;
                uninits = uninitsWhenFalse;
                scanExpr(tree.detail);
            }
            inits = initsExit;
            uninits = uninitsExit;
        }

        public void visitAssign(JCAssign tree) {
            JCTree lhs = TreeInfo.skipParens(tree.lhs);
            if (!(lhs instanceof JCIdent)) {
                scanExpr(lhs);
            }
            scanExpr(tree.rhs);
            letInit(lhs);
        }

        public void visitAssignop(JCAssignOp tree) {
            scanExpr(tree.lhs);
            scanExpr(tree.rhs);
            letInit(tree.lhs);
        }

        public void visitUnary(JCUnary tree) {
            switch (tree.getTag()) {
            case NOT:
                scanCond(tree.arg);
                Bits t = initsWhenFalse;
                initsWhenFalse = initsWhenTrue;
                initsWhenTrue = t;
                t = uninitsWhenFalse;
                uninitsWhenFalse = uninitsWhenTrue;
                uninitsWhenTrue = t;
                break;
            case PREINC: case POSTINC:
            case PREDEC: case POSTDEC:
                scanExpr(tree.arg);
                letInit(tree.arg);
                break;
            default:
                scanExpr(tree.arg);
            }
        }

        public void visitBinary(JCBinary tree) {
            switch (tree.getTag()) {
            case AND:
                scanCond(tree.lhs);
                Bits initsWhenFalseLeft = initsWhenFalse;
                Bits uninitsWhenFalseLeft = uninitsWhenFalse;
                inits = initsWhenTrue;
                uninits = uninitsWhenTrue;
                scanCond(tree.rhs);
                initsWhenFalse.andSet(initsWhenFalseLeft);
                uninitsWhenFalse.andSet(uninitsWhenFalseLeft);
                break;
            case OR:
                scanCond(tree.lhs);
                Bits initsWhenTrueLeft = initsWhenTrue;
                Bits uninitsWhenTrueLeft = uninitsWhenTrue;
                inits = initsWhenFalse;
                uninits = uninitsWhenFalse;
                scanCond(tree.rhs);
                initsWhenTrue.andSet(initsWhenTrueLeft);
                uninitsWhenTrue.andSet(uninitsWhenTrueLeft);
                break;
            default:
                scanExpr(tree.lhs);
                scanExpr(tree.rhs);
            }
        }

        public void visitIdent(JCIdent tree) {
            if (tree.sym.kind == VAR) {
                checkInit(tree.pos(), (VarSymbol)tree.sym);
                referenced(tree.sym);
            }
        }

        void referenced(Symbol sym) {
            unrefdResources.remove(sym);
        }

        public void visitTopLevel(JCCompilationUnit tree) {
            // Do nothing for TopLevel since each class is visited individually
        }

    /**************************************************************************
     * main method
     *************************************************************************/

        /** Perform definite assignment/unassignment analysis on a tree.
         */
        public void analyzeTree(Env<AttrContext> env, TreeMaker make) {
            analyzeTree(env, env.tree, make);
        }

        public void analyzeTree(Env<AttrContext> env, JCTree tree, TreeMaker make) {
            try {
                attrEnv = env;
                Flow.this.make = make;
                startPos = tree.pos().getStartPosition();
                inits = new Bits();
                uninits = new Bits();
                uninitsTry = new Bits();
                initsWhenTrue = initsWhenFalse =
                    uninitsWhenTrue = uninitsWhenFalse = null;
                if (vars == null)
                    vars = new VarSymbol[32];
                else
                    for (int i=0; i<vars.length; i++)
                        vars[i] = null;
                firstadr = 0;
                nextadr = 0;
                pendingExits = new ListBuffer<AssignPendingExit>();
                this.classDef = null;
                unrefdResources = new Scope(env.enclClass.sym);
                scan(tree);
            } finally {
                // note that recursive invocations of this method fail hard
                startPos = -1;
                inits = uninits = uninitsTry = null;
                initsWhenTrue = initsWhenFalse =
                    uninitsWhenTrue = uninitsWhenFalse = null;
                if (vars != null) for (int i=0; i<vars.length; i++)
                    vars[i] = null;
                firstadr = 0;
                nextadr = 0;
                pendingExits = null;
                Flow.this.make = null;
                this.classDef = null;
                unrefdResources = null;
            }
        }
    }

    /**
     * This pass implements the last step of the dataflow analysis, namely
     * the effectively-final analysis check. This checks that every local variable
     * reference from a lambda body/local inner class is either final or effectively final.
     * As effectively final variables are marked as such during DA/DU, this pass must run after
     * AssignAnalyzer.
     */
    class CaptureAnalyzer extends BaseAnalyzer<BaseAnalyzer.PendingExit> {

        JCTree currentTree; //local class or lambda

        @Override
        void markDead() {
            //do nothing
        }

        @SuppressWarnings("fallthrough")
        void checkEffectivelyFinal(DiagnosticPosition pos, VarSymbol sym) {
            if (currentTree != null &&
                    sym.owner.kind == MTH &&
                    sym.pos < currentTree.getStartPosition()) {
                switch (currentTree.getTag()) {
                    case CLASSDEF:
                        if (!allowEffectivelyFinalInInnerClasses) {
                            if ((sym.flags() & FINAL) == 0) {
                                reportInnerClsNeedsFinalError(pos, sym);
                            }
                            break;
                        }
                    case LAMBDA:
                        if ((sym.flags() & (EFFECTIVELY_FINAL | FINAL)) == 0) {
                           reportEffectivelyFinalError(pos, sym);
                        }
                }
            }
        }

        @SuppressWarnings("fallthrough")
        void letInit(JCTree tree) {
            tree = TreeInfo.skipParens(tree);
            if (tree.hasTag(IDENT) || tree.hasTag(SELECT)) {
                Symbol sym = TreeInfo.symbol(tree);
                if (currentTree != null &&
                        sym.kind == VAR &&
                        sym.owner.kind == MTH &&
                        ((VarSymbol)sym).pos < currentTree.getStartPosition()) {
                    switch (currentTree.getTag()) {
                        case CLASSDEF:
                            if (!allowEffectivelyFinalInInnerClasses) {
                                reportInnerClsNeedsFinalError(tree, sym);
                                break;
                            }
                        case LAMBDA:
                            reportEffectivelyFinalError(tree, sym);
                    }
                }
            }
        }

        void reportEffectivelyFinalError(DiagnosticPosition pos, Symbol sym) {
            String subKey = currentTree.hasTag(LAMBDA) ?
                  "lambda"  : "inner.cls";
            log.error(pos, "cant.ref.non.effectively.final.var", sym, diags.fragment(subKey));
        }

        void reportInnerClsNeedsFinalError(DiagnosticPosition pos, Symbol sym) {
            log.error(pos,
                    "local.var.accessed.from.icls.needs.final",
                    sym);
        }

    /*************************************************************************
     * Visitor methods for statements and definitions
     *************************************************************************/

        /* ------------ Visitor methods for various sorts of trees -------------*/

        public void visitClassDef(JCClassDecl tree) {
            JCTree prevTree = currentTree;
            try {
                currentTree = tree.sym.isLocal() ? tree : null;
                super.visitClassDef(tree);
            } finally {
                currentTree = prevTree;
            }
        }

        @Override
        public void visitLambda(JCLambda tree) {
            JCTree prevTree = currentTree;
            try {
                currentTree = tree;
                super.visitLambda(tree);
            } finally {
                currentTree = prevTree;
            }
        }

        @Override
        public void visitIdent(JCIdent tree) {
            if (tree.sym.kind == VAR) {
                checkEffectivelyFinal(tree, (VarSymbol)tree.sym);
            }
        }

        public void visitAssign(JCAssign tree) {
            JCTree lhs = TreeInfo.skipParens(tree.lhs);
            if (!(lhs instanceof JCIdent)) {
                scan(lhs);
            }
            scan(tree.rhs);
            letInit(lhs);
        }

        public void visitAssignop(JCAssignOp tree) {
            scan(tree.lhs);
            scan(tree.rhs);
            letInit(tree.lhs);
        }

        public void visitUnary(JCUnary tree) {
            switch (tree.getTag()) {
                case PREINC: case POSTINC:
                case PREDEC: case POSTDEC:
                    scan(tree.arg);
                    letInit(tree.arg);
                    break;
                default:
                    scan(tree.arg);
            }
        }

        public void visitTopLevel(JCCompilationUnit tree) {
            // Do nothing for TopLevel since each class is visited individually
        }

    /**************************************************************************
     * main method
     *************************************************************************/

        /** Perform definite assignment/unassignment analysis on a tree.
         */
        public void analyzeTree(Env<AttrContext> env, TreeMaker make) {
            analyzeTree(env, env.tree, make);
        }
        public void analyzeTree(Env<AttrContext> env, JCTree tree, TreeMaker make) {
            try {
                attrEnv = env;
                Flow.this.make = make;
                pendingExits = new ListBuffer<PendingExit>();
                scan(tree);
            } finally {
                pendingExits = null;
                Flow.this.make = null;
            }
        }
    }
}
