/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

#if !defined(JAVA2D_NO_MLIB) || defined(MLIB_ADD_SUFF)

#include "vis_AlphaMacros.h"

/***************************************************************/

const mlib_u32 vis_mul8s_tbl[] =
{
    0x0000, 0x0081, 0x0101, 0x0182,
    0x0202, 0x0283, 0x0303, 0x0384,
    0x0404, 0x0485, 0x0505, 0x0586,
    0x0606, 0x0687, 0x0707, 0x0788,
    0x0808, 0x0889, 0x0909, 0x098a,
    0x0a0a, 0x0a8b, 0x0b0b, 0x0b8c,
    0x0c0c, 0x0c8d, 0x0d0d, 0x0d8e,
    0x0e0e, 0x0e8f, 0x0f0f, 0x0f90,
    0x1010, 0x1091, 0x1111, 0x1192,
    0x1212, 0x1293, 0x1313, 0x1394,
    0x1414, 0x1495, 0x1515, 0x1596,
    0x1616, 0x1697, 0x1717, 0x1798,
    0x1818, 0x1899, 0x1919, 0x199a,
    0x1a1a, 0x1a9b, 0x1b1b, 0x1b9c,
    0x1c1c, 0x1c9d, 0x1d1d, 0x1d9e,
    0x1e1e, 0x1e9f, 0x1f1f, 0x1fa0,
    0x2020, 0x20a1, 0x2121, 0x21a2,
    0x2222, 0x22a3, 0x2323, 0x23a4,
    0x2424, 0x24a5, 0x2525, 0x25a6,
    0x2626, 0x26a7, 0x2727, 0x27a8,
    0x2828, 0x28a9, 0x2929, 0x29aa,
    0x2a2a, 0x2aab, 0x2b2b, 0x2bac,
    0x2c2c, 0x2cad, 0x2d2d, 0x2dae,
    0x2e2e, 0x2eaf, 0x2f2f, 0x2fb0,
    0x3030, 0x30b1, 0x3131, 0x31b2,
    0x3232, 0x32b3, 0x3333, 0x33b4,
    0x3434, 0x34b5, 0x3535, 0x35b6,
    0x3636, 0x36b7, 0x3737, 0x37b8,
    0x3838, 0x38b9, 0x3939, 0x39ba,
    0x3a3a, 0x3abb, 0x3b3b, 0x3bbc,
    0x3c3c, 0x3cbd, 0x3d3d, 0x3dbe,
    0x3e3e, 0x3ebf, 0x3f3f, 0x3fc0,
    0x4040, 0x40c1, 0x4141, 0x41c2,
    0x4242, 0x42c3, 0x4343, 0x43c4,
    0x4444, 0x44c5, 0x4545, 0x45c6,
    0x4646, 0x46c7, 0x4747, 0x47c8,
    0x4848, 0x48c9, 0x4949, 0x49ca,
    0x4a4a, 0x4acb, 0x4b4b, 0x4bcc,
    0x4c4c, 0x4ccd, 0x4d4d, 0x4dce,
    0x4e4e, 0x4ecf, 0x4f4f, 0x4fd0,
    0x5050, 0x50d1, 0x5151, 0x51d2,
    0x5252, 0x52d3, 0x5353, 0x53d4,
    0x5454, 0x54d5, 0x5555, 0x55d6,
    0x5656, 0x56d7, 0x5757, 0x57d8,
    0x5858, 0x58d9, 0x5959, 0x59da,
    0x5a5a, 0x5adb, 0x5b5b, 0x5bdc,
    0x5c5c, 0x5cdd, 0x5d5d, 0x5dde,
    0x5e5e, 0x5edf, 0x5f5f, 0x5fe0,
    0x6060, 0x60e1, 0x6161, 0x61e2,
    0x6262, 0x62e3, 0x6363, 0x63e4,
    0x6464, 0x64e5, 0x6565, 0x65e6,
    0x6666, 0x66e7, 0x6767, 0x67e8,
    0x6868, 0x68e9, 0x6969, 0x69ea,
    0x6a6a, 0x6aeb, 0x6b6b, 0x6bec,
    0x6c6c, 0x6ced, 0x6d6d, 0x6dee,
    0x6e6e, 0x6eef, 0x6f6f, 0x6ff0,
    0x7070, 0x70f1, 0x7171, 0x71f2,
    0x7272, 0x72f3, 0x7373, 0x73f4,
    0x7474, 0x74f5, 0x7575, 0x75f6,
    0x7676, 0x76f7, 0x7777, 0x77f8,
    0x7878, 0x78f9, 0x7979, 0x79fa,
    0x7a7a, 0x7afb, 0x7b7b, 0x7bfc,
    0x7c7c, 0x7cfd, 0x7d7d, 0x7dfe,
    0x7e7e, 0x7eff, 0x7f7f, 0x7fff,
};

/* generated by
    int i;
    for (i = 0; i < 256; i++) {
        int x = i*128.0*256.0/255.0 + 0.5;
        if (!(i & 3)) printf("\n ");
        if (x >= 0x7FFF) x = 0x7FFF;
        printf(" 0x%04x,", x);
    }
*/

/***************************************************************/

const mlib_u64 vis_div8_tbl[256 + 256] =
{
    0x020002000200ULL, 0x7fff7fff7fffULL,
    0x7fff7fff7fffULL, 0x7fff7fff7fffULL,
    0x7f807f807f80ULL, 0x660066006600ULL,
    0x550055005500ULL, 0x48db48db48dbULL,
    0x3fc03fc03fc0ULL, 0x38ab38ab38abULL,
    0x330033003300ULL, 0x2e5d2e5d2e5dULL,
    0x2a802a802a80ULL, 0x273b273b273bULL,
    0x246e246e246eULL, 0x220022002200ULL,
    0x1fe01fe01fe0ULL, 0x1e001e001e00ULL,
    0x1c551c551c55ULL, 0x1ad81ad81ad8ULL,
    0x198019801980ULL, 0x184918491849ULL,
    0x172f172f172fULL, 0x162d162d162dULL,
    0x154015401540ULL, 0x146614661466ULL,
    0x139e139e139eULL, 0x12e412e412e4ULL,
    0x123712371237ULL, 0x119611961196ULL,
    0x110011001100ULL, 0x107410741074ULL,
    0x0ff00ff00ff0ULL, 0x0f740f740f74ULL,
    0x0f000f000f00ULL, 0x0e920e920e92ULL,
    0x0e2b0e2b0e2bULL, 0x0dc90dc90dc9ULL,
    0x0d6c0d6c0d6cULL, 0x0d140d140d14ULL,
    0x0cc00cc00cc0ULL, 0x0c700c700c70ULL,
    0x0c250c250c25ULL, 0x0bdc0bdc0bdcULL,
    0x0b970b970b97ULL, 0x0b550b550b55ULL,
    0x0b160b160b16ULL, 0x0ada0ada0adaULL,
    0x0aa00aa00aa0ULL, 0x0a680a680a68ULL,
    0x0a330a330a33ULL, 0x0a000a000a00ULL,
    0x09cf09cf09cfULL, 0x099f099f099fULL,
    0x097209720972ULL, 0x094609460946ULL,
    0x091b091b091bULL, 0x08f308f308f3ULL,
    0x08cb08cb08cbULL, 0x08a508a508a5ULL,
    0x088008800880ULL, 0x085c085c085cULL,
    0x083a083a083aULL, 0x081808180818ULL,
    0x07f807f807f8ULL, 0x07d907d907d9ULL,
    0x07ba07ba07baULL, 0x079d079d079dULL,
    0x078007800780ULL, 0x076407640764ULL,
    0x074907490749ULL, 0x072f072f072fULL,
    0x071507150715ULL, 0x06fc06fc06fcULL,
    0x06e406e406e4ULL, 0x06cd06cd06cdULL,
    0x06b606b606b6ULL, 0x06a006a006a0ULL,
    0x068a068a068aULL, 0x067506750675ULL,
    0x066006600660ULL, 0x064c064c064cULL,
    0x063806380638ULL, 0x062506250625ULL,
    0x061206120612ULL, 0x060006000600ULL,
    0x05ee05ee05eeULL, 0x05dd05dd05ddULL,
    0x05cc05cc05ccULL, 0x05bb05bb05bbULL,
    0x05ab05ab05abULL, 0x059b059b059bULL,
    0x058b058b058bULL, 0x057c057c057cULL,
    0x056d056d056dULL, 0x055e055e055eULL,
    0x055005500550ULL, 0x054205420542ULL,
    0x053405340534ULL, 0x052705270527ULL,
    0x051a051a051aULL, 0x050d050d050dULL,
    0x050005000500ULL, 0x04f404f404f4ULL,
    0x04e704e704e7ULL, 0x04db04db04dbULL,
    0x04d004d004d0ULL, 0x04c404c404c4ULL,
    0x04b904b904b9ULL, 0x04ae04ae04aeULL,
    0x04a304a304a3ULL, 0x049804980498ULL,
    0x048e048e048eULL, 0x048304830483ULL,
    0x047904790479ULL, 0x046f046f046fULL,
    0x046604660466ULL, 0x045c045c045cULL,
    0x045204520452ULL, 0x044904490449ULL,
    0x044004400440ULL, 0x043704370437ULL,
    0x042e042e042eULL, 0x042504250425ULL,
    0x041d041d041dULL, 0x041404140414ULL,
    0x040c040c040cULL, 0x040404040404ULL,
    0x03fc03fc03fcULL, 0x03f403f403f4ULL,
    0x03ec03ec03ecULL, 0x03e503e503e5ULL,
    0x03dd03dd03ddULL, 0x03d603d603d6ULL,
    0x03ce03ce03ceULL, 0x03c703c703c7ULL,
    0x03c003c003c0ULL, 0x03b903b903b9ULL,
    0x03b203b203b2ULL, 0x03ab03ab03abULL,
    0x03a503a503a5ULL, 0x039e039e039eULL,
    0x039703970397ULL, 0x039103910391ULL,
    0x038b038b038bULL, 0x038403840384ULL,
    0x037e037e037eULL, 0x037803780378ULL,
    0x037203720372ULL, 0x036c036c036cULL,
    0x036603660366ULL, 0x036103610361ULL,
    0x035b035b035bULL, 0x035503550355ULL,
    0x035003500350ULL, 0x034a034a034aULL,
    0x034503450345ULL, 0x034003400340ULL,
    0x033a033a033aULL, 0x033503350335ULL,
    0x033003300330ULL, 0x032b032b032bULL,
    0x032603260326ULL, 0x032103210321ULL,
    0x031c031c031cULL, 0x031703170317ULL,
    0x031303130313ULL, 0x030e030e030eULL,
    0x030903090309ULL, 0x030503050305ULL,
    0x030003000300ULL, 0x02fc02fc02fcULL,
    0x02f702f702f7ULL, 0x02f302f302f3ULL,
    0x02ee02ee02eeULL, 0x02ea02ea02eaULL,
    0x02e602e602e6ULL, 0x02e202e202e2ULL,
    0x02dd02dd02ddULL, 0x02d902d902d9ULL,
    0x02d502d502d5ULL, 0x02d102d102d1ULL,
    0x02cd02cd02cdULL, 0x02c902c902c9ULL,
    0x02c602c602c6ULL, 0x02c202c202c2ULL,
    0x02be02be02beULL, 0x02ba02ba02baULL,
    0x02b602b602b6ULL, 0x02b302b302b3ULL,
    0x02af02af02afULL, 0x02ac02ac02acULL,
    0x02a802a802a8ULL, 0x02a402a402a4ULL,
    0x02a102a102a1ULL, 0x029e029e029eULL,
    0x029a029a029aULL, 0x029702970297ULL,
    0x029302930293ULL, 0x029002900290ULL,
    0x028d028d028dULL, 0x028a028a028aULL,
    0x028602860286ULL, 0x028302830283ULL,
    0x028002800280ULL, 0x027d027d027dULL,
    0x027a027a027aULL, 0x027702770277ULL,
    0x027402740274ULL, 0x027102710271ULL,
    0x026e026e026eULL, 0x026b026b026bULL,
    0x026802680268ULL, 0x026502650265ULL,
    0x026202620262ULL, 0x025f025f025fULL,
    0x025c025c025cULL, 0x025a025a025aULL,
    0x025702570257ULL, 0x025402540254ULL,
    0x025102510251ULL, 0x024f024f024fULL,
    0x024c024c024cULL, 0x024902490249ULL,
    0x024702470247ULL, 0x024402440244ULL,
    0x024202420242ULL, 0x023f023f023fULL,
    0x023d023d023dULL, 0x023a023a023aULL,
    0x023802380238ULL, 0x023502350235ULL,
    0x023302330233ULL, 0x023002300230ULL,
    0x022e022e022eULL, 0x022c022c022cULL,
    0x022902290229ULL, 0x022702270227ULL,
    0x022502250225ULL, 0x022202220222ULL,
    0x022002200220ULL, 0x021e021e021eULL,
    0x021c021c021cULL, 0x021902190219ULL,
    0x021702170217ULL, 0x021502150215ULL,
    0x021302130213ULL, 0x021102110211ULL,
    0x020e020e020eULL, 0x020c020c020cULL,
    0x020a020a020aULL, 0x020802080208ULL,
    0x020602060206ULL, 0x020402040204ULL,
    0x020202020202ULL, 0x020002000200ULL,

    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
    0x020002000200ULL, 0x020002000200ULL,
};

/* generated by
    int i;
    for (i = 0; i < 256 + 256; i++) {
        int ii = (i == 0 || i > 255) ? 255 : i;
        int x = 512.0*(255.0/ii) + 0.5;
        if (!(i & 1)) printf("\n ");
        if (x >= 0x7FFF) x = 0x7FFF;
        printf(" 0x%04x%04x%04xULL,", x, x, x);
    }
*/

/***************************************************************/

const mlib_u64 vis_div8pre_tbl[256] =
{
    0x0100010001000100ULL, 0x01007fff7fff7fffULL,
    0x01007f807f807f80ULL, 0x0100550055005500ULL,
    0x01003fc03fc03fc0ULL, 0x0100330033003300ULL,
    0x01002a802a802a80ULL, 0x0100246e246e246eULL,
    0x01001fe01fe01fe0ULL, 0x01001c551c551c55ULL,
    0x0100198019801980ULL, 0x0100172f172f172fULL,
    0x0100154015401540ULL, 0x0100139e139e139eULL,
    0x0100123712371237ULL, 0x0100110011001100ULL,
    0x01000ff00ff00ff0ULL, 0x01000f000f000f00ULL,
    0x01000e2b0e2b0e2bULL, 0x01000d6c0d6c0d6cULL,
    0x01000cc00cc00cc0ULL, 0x01000c250c250c25ULL,
    0x01000b970b970b97ULL, 0x01000b160b160b16ULL,
    0x01000aa00aa00aa0ULL, 0x01000a330a330a33ULL,
    0x010009cf09cf09cfULL, 0x0100097209720972ULL,
    0x0100091b091b091bULL, 0x010008cb08cb08cbULL,
    0x0100088008800880ULL, 0x0100083a083a083aULL,
    0x010007f807f807f8ULL, 0x010007ba07ba07baULL,
    0x0100078007800780ULL, 0x0100074907490749ULL,
    0x0100071507150715ULL, 0x010006e406e406e4ULL,
    0x010006b606b606b6ULL, 0x0100068a068a068aULL,
    0x0100066006600660ULL, 0x0100063806380638ULL,
    0x0100061206120612ULL, 0x010005ee05ee05eeULL,
    0x010005cc05cc05ccULL, 0x010005ab05ab05abULL,
    0x0100058b058b058bULL, 0x0100056d056d056dULL,
    0x0100055005500550ULL, 0x0100053405340534ULL,
    0x0100051a051a051aULL, 0x0100050005000500ULL,
    0x010004e704e704e7ULL, 0x010004d004d004d0ULL,
    0x010004b904b904b9ULL, 0x010004a304a304a3ULL,
    0x0100048e048e048eULL, 0x0100047904790479ULL,
    0x0100046604660466ULL, 0x0100045204520452ULL,
    0x0100044004400440ULL, 0x0100042e042e042eULL,
    0x0100041d041d041dULL, 0x0100040c040c040cULL,
    0x010003fc03fc03fcULL, 0x010003ec03ec03ecULL,
    0x010003dd03dd03ddULL, 0x010003ce03ce03ceULL,
    0x010003c003c003c0ULL, 0x010003b203b203b2ULL,
    0x010003a503a503a5ULL, 0x0100039703970397ULL,
    0x0100038b038b038bULL, 0x0100037e037e037eULL,
    0x0100037203720372ULL, 0x0100036603660366ULL,
    0x0100035b035b035bULL, 0x0100035003500350ULL,
    0x0100034503450345ULL, 0x0100033a033a033aULL,
    0x0100033003300330ULL, 0x0100032603260326ULL,
    0x0100031c031c031cULL, 0x0100031303130313ULL,
    0x0100030903090309ULL, 0x0100030003000300ULL,
    0x010002f702f702f7ULL, 0x010002ee02ee02eeULL,
    0x010002e602e602e6ULL, 0x010002dd02dd02ddULL,
    0x010002d502d502d5ULL, 0x010002cd02cd02cdULL,
    0x010002c602c602c6ULL, 0x010002be02be02beULL,
    0x010002b602b602b6ULL, 0x010002af02af02afULL,
    0x010002a802a802a8ULL, 0x010002a102a102a1ULL,
    0x0100029a029a029aULL, 0x0100029302930293ULL,
    0x0100028d028d028dULL, 0x0100028602860286ULL,
    0x0100028002800280ULL, 0x0100027a027a027aULL,
    0x0100027402740274ULL, 0x0100026e026e026eULL,
    0x0100026802680268ULL, 0x0100026202620262ULL,
    0x0100025c025c025cULL, 0x0100025702570257ULL,
    0x0100025102510251ULL, 0x0100024c024c024cULL,
    0x0100024702470247ULL, 0x0100024202420242ULL,
    0x0100023d023d023dULL, 0x0100023802380238ULL,
    0x0100023302330233ULL, 0x0100022e022e022eULL,
    0x0100022902290229ULL, 0x0100022502250225ULL,
    0x0100022002200220ULL, 0x0100021c021c021cULL,
    0x0100021702170217ULL, 0x0100021302130213ULL,
    0x0100020e020e020eULL, 0x0100020a020a020aULL,
    0x0100020602060206ULL, 0x0100020202020202ULL,
    0x010001fe01fe01feULL, 0x010001fa01fa01faULL,
    0x010001f601f601f6ULL, 0x010001f201f201f2ULL,
    0x010001ef01ef01efULL, 0x010001eb01eb01ebULL,
    0x010001e701e701e7ULL, 0x010001e401e401e4ULL,
    0x010001e001e001e0ULL, 0x010001dc01dc01dcULL,
    0x010001d901d901d9ULL, 0x010001d601d601d6ULL,
    0x010001d201d201d2ULL, 0x010001cf01cf01cfULL,
    0x010001cc01cc01ccULL, 0x010001c901c901c9ULL,
    0x010001c501c501c5ULL, 0x010001c201c201c2ULL,
    0x010001bf01bf01bfULL, 0x010001bc01bc01bcULL,
    0x010001b901b901b9ULL, 0x010001b601b601b6ULL,
    0x010001b301b301b3ULL, 0x010001b001b001b0ULL,
    0x010001ad01ad01adULL, 0x010001ab01ab01abULL,
    0x010001a801a801a8ULL, 0x010001a501a501a5ULL,
    0x010001a201a201a2ULL, 0x010001a001a001a0ULL,
    0x0100019d019d019dULL, 0x0100019b019b019bULL,
    0x0100019801980198ULL, 0x0100019501950195ULL,
    0x0100019301930193ULL, 0x0100019001900190ULL,
    0x0100018e018e018eULL, 0x0100018c018c018cULL,
    0x0100018901890189ULL, 0x0100018701870187ULL,
    0x0100018501850185ULL, 0x0100018201820182ULL,
    0x0100018001800180ULL, 0x0100017e017e017eULL,
    0x0100017c017c017cULL, 0x0100017901790179ULL,
    0x0100017701770177ULL, 0x0100017501750175ULL,
    0x0100017301730173ULL, 0x0100017101710171ULL,
    0x0100016f016f016fULL, 0x0100016d016d016dULL,
    0x0100016b016b016bULL, 0x0100016901690169ULL,
    0x0100016701670167ULL, 0x0100016501650165ULL,
    0x0100016301630163ULL, 0x0100016101610161ULL,
    0x0100015f015f015fULL, 0x0100015d015d015dULL,
    0x0100015b015b015bULL, 0x0100015901590159ULL,
    0x0100015801580158ULL, 0x0100015601560156ULL,
    0x0100015401540154ULL, 0x0100015201520152ULL,
    0x0100015001500150ULL, 0x0100014f014f014fULL,
    0x0100014d014d014dULL, 0x0100014b014b014bULL,
    0x0100014a014a014aULL, 0x0100014801480148ULL,
    0x0100014601460146ULL, 0x0100014501450145ULL,
    0x0100014301430143ULL, 0x0100014201420142ULL,
    0x0100014001400140ULL, 0x0100013e013e013eULL,
    0x0100013d013d013dULL, 0x0100013b013b013bULL,
    0x0100013a013a013aULL, 0x0100013801380138ULL,
    0x0100013701370137ULL, 0x0100013501350135ULL,
    0x0100013401340134ULL, 0x0100013201320132ULL,
    0x0100013101310131ULL, 0x0100013001300130ULL,
    0x0100012e012e012eULL, 0x0100012d012d012dULL,
    0x0100012b012b012bULL, 0x0100012a012a012aULL,
    0x0100012901290129ULL, 0x0100012701270127ULL,
    0x0100012601260126ULL, 0x0100012501250125ULL,
    0x0100012301230123ULL, 0x0100012201220122ULL,
    0x0100012101210121ULL, 0x0100012001200120ULL,
    0x0100011e011e011eULL, 0x0100011d011d011dULL,
    0x0100011c011c011cULL, 0x0100011b011b011bULL,
    0x0100011901190119ULL, 0x0100011801180118ULL,
    0x0100011701170117ULL, 0x0100011601160116ULL,
    0x0100011501150115ULL, 0x0100011301130113ULL,
    0x0100011201120112ULL, 0x0100011101110111ULL,
    0x0100011001100110ULL, 0x0100010f010f010fULL,
    0x0100010e010e010eULL, 0x0100010d010d010dULL,
    0x0100010c010c010cULL, 0x0100010a010a010aULL,
    0x0100010901090109ULL, 0x0100010801080108ULL,
    0x0100010701070107ULL, 0x0100010601060106ULL,
    0x0100010501050105ULL, 0x0100010401040104ULL,
    0x0100010301030103ULL, 0x0100010201020102ULL,
    0x0100010101010101ULL, 0x0100010001000100ULL,
};

/* generated by
    int i;
    for (i = 0; i < 256; i++) {
        int ii = (i == 0 || i > 255) ? 255 : i;
        int x = 256.0*(255.0/ii) + 0.5;
        if (!(i & 1)) printf("\n ");
        if (x >= 0x7FFF) x = 0x7FFF;
        printf(" 0x%04x%04x%04x%04xULL,", 256, x, x, x);
    }
*/

/***************************************************************/

#endif
