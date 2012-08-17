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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cbpaine
 */
import java.util.Calendar;

public abstract class TimedEventAdapter implements TimedEvent
{
    protected volatile boolean active = false;
    protected int dividefield, divisor;
    protected int offsetfield, offset;
    protected Calendar next = Calendar.getInstance();

    public TimedEventAdapter(int dividefield, int divisor)
    {
        this.dividefield = dividefield;
        this.divisor = divisor;
        this.offsetfield = -1;
        this.offset = 0;
    }

    public TimedEventAdapter(int dividefield, int divisor, int offsetfield, int offset)
    {
        this.dividefield = dividefield;
        this.divisor = divisor;
        this.offsetfield = offsetfield;
        this.offset = offset;
    }

    public TimedEventAdapter()
    {
        this(Calendar.MINUTE, 1);
    }

    public void destroy()
    {
        TimedEventManager.getManager().removeTimedEventListener(this);
    }

    @Override
    protected void finalize() throws Throwable
    {
        try { destroy(); }
        finally { super.finalize(); }
    }

    ///////////////////////////////////////////////////////////////////////////
    // TimedEvent interface
    ///////////////////////////////////////////////////////////////////////////

    public Calendar getNext()
    {
        return next;
    }

    public void setNext(Calendar cal)
    {
        next = cal;
    }

    public int getDivideField()
    {
        return dividefield;
    }

    public void setDivideField(int field)
    {
        dividefield = field;
    }

    public int getDivisor()
    {
        return divisor;
    }

    public void setDivisor(int div)
    {
        divisor = div;
    }

    public int getOffsetField()
    {
        return offsetfield;
    }

    public void setOffsetField(int field)
    {
        offsetfield = field;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int off)
    {
        offset = off;
    }

    public abstract void doEvent();

    public synchronized void setActive(boolean state)
    {
        active = state;
    }

    public synchronized boolean isActive()
    {
        return active;
    }
}
