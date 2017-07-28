package ch.wisv.events.utils;

/**
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public enum LDAPGroupEnum {

    AKCIE("akcie"),
    ANNUCIE("annucie"),
    BT("bedrijvenreis"),
    BEHEER("chbeheer"),
    CHIPCIE("chipcie"),
    CHOCO("choco"),
    COMMA("comma"),
    DIES("dies"),
    FACIE("facie"),
    FILMCREW("filmcrew"),
    FLITCIE("flitcie"),
    GALA("gala"),
    GALACIE("glacie"),
    ICOM("icom"),
    LANCIE("lancie"),
    LUCIE("lucie"),
    MACHAZINE("machazine"),
    MAPHYA("maphya"),
    MATCH("match"),
    MEISCIE("meiscie"),
    REISCOMMISSIE("reiscommissie"),
    SJAARCIES("sjaarcie"),
    SYMPOSIUM("symposium"),
    VERDIEPCIE("verdiepcie"),
    W3CIE("w3cie"),
    WIFI("wifi"),
    WOCKY("wocky"),
    BESTUUR("bestuur"),
    VOORZITTER("voorzitter"),
    SECRETARIS("secretaris"),
    PENNINGMEESTER("penningmeester"),
    COW("cow"),
    COI("coi"),
    CPR("cpr"),
    CC("cc");

    /**
     * Field name
     */
    private final String name;

    /**
     * Constructor LDAPGroupEnum creates a new LDAPGroupEnum instance.
     *
     * @param name of type String
     */
    LDAPGroupEnum(String name) {
        this.name = name;
    }

    /**
     * Method getName returns the name of this LDAPGroupEnum object.
     *
     * @return the name (type String) of this LDAPGroupEnum object.
     */
    public String getName() {
        return name;
    }

}
