/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.domain.model.libraries

enum class LicenseType(
    val licenseName: String,
    val url: String,
    val description: String,
) {
    ApacheV2(
        licenseName = "Apache-2.0",
        description = /* "Copyright $year $copyrightOwners" */ "\n" +
                "\n" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");" +
                " you may not use this file except in compliance with the License." +
                " You may obtain a copy of the License at" +
                "\n" +
                "\thttps://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software" +
                " distributed under the License is distributed on an \"AS IS\" BASIS," +
                " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                " See the License for the specific language governing permissions and" +
                " limitations under the License.",
        url = "https://www.apache.org/licenses/LICENSE-2.0",
    ),
    FreeBSD(
        licenseName = "BSD",
        description = /* "Copyright (c) $year, $copyrightOwners" */ "\n" +
                "\n" +
                "Redistribution and use in source and binary forms, with or without modification," +
                " are permitted provided that the following conditions are met:\n" +
                "\n" +
                "Redistributions of source code must retain the above copyright notice," +
                " this list of conditions and the following disclaimer.\n" +
                "Redistributions in binary form must reproduce the above copyright notice," +
                " this list of conditions and the following disclaimer in the documentation" +
                " and/or other materials provided with the distribution.\n" +
                "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS" +
                " \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO," +
                " THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE" +
                " ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE" +
                " FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL" +
                " DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR" +
                " SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER" +
                " CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY," +
                " OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE" +
                " OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.",
        url = "https://www.freebsd.org/copyright/freebsd-license/",
    ),
    EclipsePublicLicenseV2(
        licenseName = "Eclipse Public License - v 2.0",
        description = "THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC LICENSE (“AGREEMENT”). ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.\n" +
                "\n" +
                "1. DEFINITIONS\n" +
                "“Contribution” means:\n" +
                "\n" +
                "\ta) in the case of the initial Contributor, the initial content Distributed under this Agreement, and\n" +
                "\tb) in the case of each subsequent Contributor:\n" +
                "\t\ti) changes to the Program, and\n" +
                "\t\tii) additions to the Program;\n" +
                "where such changes and/or additions to the Program originate from and are Distributed by that particular Contributor. A Contribution “originates” from a Contributor if it was added to the Program by such Contributor itself or anyone acting on such Contributor's behalf. Contributions do not include changes or additions to the Program that are not Modified Works.\n" +
                "“Contributor” means any person or entity that Distributes the Program.\n" +
                "\n" +
                "“Licensed Patents” mean patent claims licensable by a Contributor which are necessarily infringed by the use or sale of its Contribution alone or when combined with the Program.\n" +
                "\n" +
                "“Program” means the Contributions Distributed in accordance with this Agreement.\n" +
                "\n" +
                "“Recipient” means anyone who receives the Program under this Agreement or any Secondary License (as applicable), including Contributors.\n" +
                "\n" +
                "“Derivative Works” shall mean any work, whether in Source Code or other form, that is based on (or derived from) the Program and for which the editorial revisions, annotations, elaborations, or other modifications represent, as a whole, an original work of authorship.\n" +
                "\n" +
                "“Modified Works” shall mean any work in Source Code or other form that results from an addition to, deletion from, or modification of the contents of the Program, including, for purposes of clarity any new file in Source Code form that contains any contents of the Program. Modified Works shall not include works that contain only declarations, interfaces, types, classes, structures, or files of the Program solely in each case in order to link to, bind by name, or subclass the Program or Modified Works thereof.\n" +
                "\n" +
                "“Distribute” means the acts of a) distributing or b) making available in any manner that enables the transfer of a copy.\n" +
                "\n" +
                "“Source Code” means the form of a Program preferred for making modifications, including but not limited to software source code, documentation source, and configuration files.\n" +
                "\n" +
                "“Secondary License” means either the GNU General Public License, Version 2.0, or any later versions of that license, including any exceptions or additional permissions as identified by the initial Contributor.\n" +
                "\n" +
                "2. GRANT OF RIGHTS\n" +
                "\ta) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform, Distribute and sublicense the Contribution of such Contributor, if any, and such Derivative Works.\n" +
                "\tb) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free patent license under Licensed Patents to make, use, sell, offer to sell, import and otherwise transfer the Contribution of such Contributor, if any, in Source Code or other form. This patent license shall apply to the combination of the Contribution and the Program if, at the time the Contribution is added by the Contributor, such addition of the Contribution causes such combination to be covered by the Licensed Patents. The patent license shall not apply to any other combinations which include the Contribution. No hardware per se is licensed hereunder.\n" +
                "\tc) Recipient understands that although each Contributor grants the licenses to its Contributions set forth herein, no assurances are provided by any Contributor that the Program does not infringe the patent or other intellectual property rights of any other entity. Each Contributor disclaims any liability to Recipient for claims brought by any other entity based on infringement of intellectual property rights or otherwise. As a condition to exercising the rights and licenses granted hereunder, each Recipient hereby assumes sole responsibility to secure any other intellectual property rights needed, if any. For example, if a third party patent license is required to allow Recipient to Distribute the Program, it is Recipient's responsibility to acquire that license before distributing the Program.\n" +
                "\td) Each Contributor represents that to its knowledge it has sufficient copyright rights in its Contribution, if any, to grant the copyright license set forth in this Agreement.\n" +
                "\te) Notwithstanding the terms of any Secondary License, no Contributor makes additional grants to any Recipient (other than those set forth in this Agreement) as a result of such Recipient's receipt of the Program under the terms of a Secondary License (if permitted under the terms of Section 3).\n" +
                "3. REQUIREMENTS\n" +
                "3.1 If a Contributor Distributes the Program in any form, then:\n" +
                "\n" +
                "\ta) the Program must also be made available as Source Code, in accordance with section 3.2, and the Contributor must accompany the Program with a statement that the Source Code for the Program is available under this Agreement, and informs Recipients how to obtain it in a reasonable manner on or through a medium customarily used for software exchange; and\n" +
                "\tb) the Contributor may Distribute the Program under a license different than this Agreement, provided that such license:\n" +
                "\t\ti) effectively disclaims on behalf of all other Contributors all warranties and conditions, express and implied, including warranties or conditions of title and non-infringement, and implied warranties or conditions of merchantability and fitness for a particular purpose;\n" +
                "\t\tii) effectively excludes on behalf of all other Contributors all liability for damages, including direct, indirect, special, incidental and consequential damages, such as lost profits;\n" +
                "\t\tiii) does not attempt to limit or alter the recipients' rights in the Source Code under section 3.2; and\n" +
                "\t\tiv) requires any subsequent distribution of the Program by any party to be under a license that satisfies the requirements of this section 3.\n" +
                "3.2 When the Program is Distributed as Source Code:\n" +
                "\n" +
                "\ta) it must be made available under this Agreement, or if the Program (i) is combined with other material in a separate file or files made available under a Secondary License, and (ii) the initial Contributor attached to the Source Code the notice described in Exhibit A of this Agreement, then the Program may be made available under the terms of such Secondary Licenses, and\n" +
                "\tb) a copy of this Agreement must be included with each copy of the Program.\n" +
                "3.3 Contributors may not remove or alter any copyright, patent, trademark, attribution notices, disclaimers of warranty, or limitations of liability (‘notices’) contained within the Program from any copy of the Program which they Distribute, provided that Contributors may add their own appropriate notices.\n" +
                "\n" +
                "4. COMMERCIAL DISTRIBUTION\n" +
                "Commercial distributors of software may accept certain responsibilities with respect to end users, business partners and the like. While this license is intended to facilitate the commercial use of the Program, the Contributor who includes the Program in a commercial product offering should do so in a manner which does not create potential liability for other Contributors. Therefore, if a Contributor includes the Program in a commercial product offering, such Contributor (“Commercial Contributor”) hereby agrees to defend and indemnify every other Contributor (“Indemnified Contributor”) against any losses, damages and costs (collectively “Losses”) arising from claims, lawsuits and other legal actions brought by a third party against the Indemnified Contributor to the extent caused by the acts or omissions of such Commercial Contributor in connection with its distribution of the Program in a commercial product offering. The obligations in this section do not apply to any claims or Losses relating to any actual or alleged intellectual property infringement. In order to qualify, an Indemnified Contributor must: a) promptly notify the Commercial Contributor in writing of such claim, and b) allow the Commercial Contributor to control, and cooperate with the Commercial Contributor in, the defense and any related settlement negotiations. The Indemnified Contributor may participate in any such claim at its own expense.\n" +
                "\n" +
                "For example, a Contributor might include the Program in a commercial product offering, Product X. That Contributor is then a Commercial Contributor. If that Commercial Contributor then makes performance claims, or offers warranties related to Product X, those performance claims and warranties are such Commercial Contributor's responsibility alone. Under this section, the Commercial Contributor would have to defend claims against the other Contributors related to those performance claims and warranties, and if a court requires any other Contributor to pay any damages as a result, the Commercial Contributor must pay those damages.\n" +
                "\n" +
                "5. NO WARRANTY\n" +
                "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, AND TO THE EXTENT PERMITTED BY APPLICABLE LAW, THE PROGRAM IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Each Recipient is solely responsible for determining the appropriateness of using and distributing the Program and assumes all risks associated with its exercise of rights under this Agreement, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and unavailability or interruption of operations.\n" +
                "\n" +
                "6. DISCLAIMER OF LIABILITY\n" +
                "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, AND TO THE EXTENT PERMITTED BY APPLICABLE LAW, NEITHER RECIPIENT NOR ANY CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.\n" +
                "\n" +
                "7. GENERAL\n" +
                "If any provision of this Agreement is invalid or unenforceable under applicable law, it shall not affect the validity or enforceability of the remainder of the terms of this Agreement, and without further action by the parties hereto, such provision shall be reformed to the minimum extent necessary to make such provision valid and enforceable.\n" +
                "\n" +
                "If Recipient institutes patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit) alleging that the Program itself (excluding combinations of the Program with other software or hardware) infringes such Recipient's patent(s), then such Recipient's rights granted under Section 2(b) shall terminate as of the date such litigation is filed.\n" +
                "\n" +
                "All Recipient's rights under this Agreement shall terminate if it fails to comply with any of the material terms or conditions of this Agreement and does not cure such failure in a reasonable period of time after becoming aware of such noncompliance. If all Recipient's rights under this Agreement terminate, Recipient agrees to cease use and distribution of the Program as soon as reasonably practicable. However, Recipient's obligations under this Agreement and any licenses granted by Recipient relating to the Program shall continue and survive.\n" +
                "\n" +
                "Everyone is permitted to copy and distribute copies of this Agreement, but in order to avoid inconsistency the Agreement is copyrighted and may only be modified in the following manner. The Agreement Steward reserves the right to publish new versions (including revisions) of this Agreement from time to time. No one other than the Agreement Steward has the right to modify this Agreement. The Eclipse Foundation is the initial Agreement Steward. The Eclipse Foundation may assign the responsibility to serve as the Agreement Steward to a suitable separate entity. Each new version of the Agreement will be given a distinguishing version number. The Program (including Contributions) may always be Distributed subject to the version of the Agreement under which it was received. In addition, after a new version of the Agreement is published, Contributor may elect to Distribute the Program (including its Contributions) under the new version.\n" +
                "\n" +
                "Except as expressly stated in Sections 2(a) and 2(b) above, Recipient receives no rights or licenses to the intellectual property of any Contributor under this Agreement, whether expressly, by implication, estoppel or otherwise. All rights in the Program not expressly granted under this Agreement are reserved. Nothing in this Agreement is intended to be enforceable by any entity that is not a Contributor or Recipient. No third-party beneficiary rights are created under this Agreement.\n" +
                "\n" +
                "Exhibit A – Form of Secondary Licenses Notice\n" +
                "“This Source Code may also be made available under the following Secondary Licenses when the conditions for such availability set forth in the Eclipse Public License, v. 2.0 are satisfied: {name license(s), version(s), and exceptions or additional permissions here}.”\n" +
                "\n" +
                "Simply including a copy of this Agreement, including this Exhibit A is not sufficient to license the Source Code under Secondary Licenses.\n" +
                "\n" +
                "If it is not possible or desirable to put the notice in a particular file, then You may include the notice in a location (such as a LICENSE file in a relevant directory) where a recipient would be likely to look for such a notice.\n" +
                "\n" +
                "You may add additional accurate notices of copyright ownership.",
        url = "https://www.eclipse.org/legal/epl-2.0/"
    ),
    GPLV2(
        licenseName = "GNU General Public License, version 2",
        description = /* "Copyright (C) $year $copyrightOwners" */ "\n" +
                "\n" +
                "This program is free software; you can redistribute it and/or" +
                " modify it under the terms of the GNU General Public License" +
                " as published by the Free Software Foundation; either version 2" +
                " of the License, or (at your option) any later version.\n" +
                "\n" +
                "This program is distributed in the hope that it will be useful," +
                " but WITHOUT ANY WARRANTY; without even the implied warranty of" +
                " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" +
                " GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License" +
                " along with this program; if not, write to the Free Software" +
                " Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.",
        url = "https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"
    ),
    GPLV3(
        licenseName = "GNU General Public License v3.0",
        description = /* "Copyright (C) $year $copyrightOwners" */ "\n" +
                "\n" +
                "This program is free software: you can redistribute it and/or modify" +
                " it under the terms of the GNU General Public License as published by" +
                " the Free Software Foundation, either version 3 of the License, or" +
                " (at your option) any later version.\n" +
                "\n" +
                "This program is distributed in the hope that it will be useful," +
                " but WITHOUT ANY WARRANTY; without even the implied warranty of" +
                " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" +
                " GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License" +
                " along with this program. If not, see <https://www.gnu.org/licenses/>.",
        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
    ),
    JayGPLV3(
        licenseName = "GNU General Public License v3.0",
        description = /* "Copyright (C) $year $copyrightOwners" */ "\n" +
                "\n" +
                "Jay is a driver behaviour analytics app.\n" +
                "\n" +
                "This file is part of Jay.\n" +
                "\n" +
                "Jay is free software: you can redistribute it and/or modify" +
                " it under the terms of the GNU General Public License as published by" +
                " the Free Software Foundation, either version 3 of the License, or" +
                " (at your option) any later version.\n" +
                "\n" +
                "Jay is distributed in the hope that it will be useful," +
                " but WITHOUT ANY WARRANTY; without even the implied warranty of" +
                " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the" +
                " GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License" +
                " along with Jay. If not, see <https://www.gnu.org/licenses/>.",
        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
    )
}