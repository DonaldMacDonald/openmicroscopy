/*
 * org.openmicroscopy.shoola.env.event.ResponseEvent
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.event;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Generic to type to represent the completion of an asynchronous operation. 
 * A concrete subclass encapsulates the result of the operation.
 * Every <code>ResponseEvent</code> object is linked to the 
 * <code>RequestEvent</code> object that originated it.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *               a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class ResponseEvent 
    extends AgentEvent
{
    
    /** The object that originated this event. */
    private RequestEvent    act;
    
    /**
     * Creates a new instance.
     * 
     * @param act The object that originated this event.
     */
    protected ResponseEvent(RequestEvent act)
    {
        this.act = act;
    }
    
    /** Represents the completion of an operation. */
    public void complete()
    {
        if (act != null) act.handleCompletion(this);
    }
    
    /**
     * Returns the {@link RequestEvent} that originated this event.
     * 
     * @return See above.
     */
    public RequestEvent getACT() { return act; }
    
}
