/*
 * Copyright (C) 2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.container.context;

/**
 * <p>
 * Thrown when a definition error occurs.
 * </p>
 * 
 * <p>
 * Definition errors are developer errors. They may be detected by tooling at development time, and are also detected by the
 * container at initialization time. If a definition error exists in a deployment, initialization will be aborted by the
 * container.
 * </p>
 * 
 * <p>
 * The container is permitted to define a non-portable mode, for use at development time, in which some definition errors do not
 * cause application initialization to abort.
 * </p>
 * 
 * <p>
 * An implementation is permitted to throw a subclass of {@link DefinitionException} for any definition error which exists.
 * </p>
 * 
 * @author Pete Muir
 * @since 1.1
 */
public class DefinitionException extends RuntimeException {

    private static final long serialVersionUID = -2699170549782567339L;

    public DefinitionException(String message, Throwable t) {
        super(message, t);
    }

    public DefinitionException(String message) {
        super(message);
    }

    public DefinitionException(Throwable t) {
        super(t);
    }

}
