/*
    This file is part of Flywire.

    Flywire is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Flywire is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Flywire.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.bentokit.flywire.event;

public interface TimedEvent
{
    public java.util.Calendar getNext();
    public int getDivideField();
    public int getDivisor();
    public int getOffsetField();
    public int getOffset();

    public void setNext(java.util.Calendar cal);
    public void setDivideField(int field);
    public void setDivisor(int div);
    public void setOffsetField(int field);
    public void setOffset(int off);

    public void doEvent();

    public void setActive(boolean state);
    public boolean isActive();
}
