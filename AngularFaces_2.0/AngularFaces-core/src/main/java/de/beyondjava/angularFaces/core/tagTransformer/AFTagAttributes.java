/**
 *  (C) 2013-2014 Stephan Rauh http://www.beyondjava.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.beyondjava.angularFaces.core.tagTransformer;
/** This class is an adapted version of the DefaultTagDecorator from the Mojarra library (jsf-impl-2.2.7).
 * Changes (C) 2014 by Stephan Rauh (http://www.beyondjava.net).
 */


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Parts of this file are subject to Copyright [2014] [Stephan Rauh] */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.view.Location;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributes;

/**
 * A set of TagAttributesImpl, usually representing all attributes on a Tag.
 * 
 * @see javax.faces.view.facelets.TagAttribute
 * @author Jacob Hookom
 * @version $Id: TagAttributesImpl.java 10331 2012-07-25 13:26:07Z mriem $
 */
public final class AFTagAttributes extends TagAttributes {
    private final static TagAttribute[] EMPTY = new TagAttribute[0];

    private TagAttribute[] attrs;

    private String[] ns;

    private List nsattrs;
    
    private Tag tag;

    /**
     * 
     */
    public AFTagAttributes(TagAttribute[] attrs) {
        init(attrs);
    }

	private void init(TagAttribute[] attrs) {
		this.attrs = attrs;

        // grab namespaces
        int i = 0;
        Set<String> set = new HashSet<String>();
        for (i = 0; i < this.attrs.length; i++) {
            set.add(this.attrs[i].getNamespace());
        }
        this.ns = set.toArray(new String[set.size()]);
        Arrays.sort(ns);

        // assign attrs
        this.nsattrs = new ArrayList<Object[]>();
        for (i = 0; i < ns.length; i++) {
            nsattrs.add(i, new ArrayList());
        }
        int nsIdx = 0;
        for (i = 0; i < this.attrs.length; i++) {
            nsIdx = Arrays.binarySearch(ns, this.attrs[i].getNamespace());
            ((List) nsattrs.get(nsIdx)).add(this.attrs[i]);
        }
        for (i = 0; i < ns.length; i++) {
            List r = (List) nsattrs.get(i);
            nsattrs.set(i, r.toArray(new TagAttribute[r.size()]));
        }
	}
	
	public void addAttribute(Location location, String ns, String myLocalName, String qName, String value) {
		TagAttribute[] newAttrs = Arrays.copyOf(attrs, attrs.length+1);
		newAttrs[attrs.length] = TagAttributeUtilities.createTagAttribute(location, ns, myLocalName, qName, value);
		init(newAttrs);
	}

    /**
     * Return an array of all TagAttributesImpl in this set
     * 
     * @return a non-null array of TagAttributesImpl
     */
    @Override
    public TagAttribute[] getAll() {
        return this.attrs;
    }

    /**
     * Using no namespace, find the TagAttribute
     * 
     * @see #get(String, String)
     * @param localName
     *            tag attribute name
     * @return the TagAttribute found, otherwise null
     */
    @Override
    public TagAttribute get(String localName) {
        return get("", localName);
    }

    /**
     * Find a TagAttribute that matches the passed namespace and local name.
     * 
     * @param ns
     *            namespace of the desired attribute
     * @param localName
     *            local name of the attribute
     * @return a TagAttribute found, otherwise null
     */
    @Override
    public TagAttribute get(String ns, String localName) {
        if (ns != null && localName != null) {
            int idx = Arrays.binarySearch(this.ns, ns);
            if (idx >= 0) {
                TagAttribute[] uia = (TagAttribute[]) this.nsattrs.get(idx);
                for (int i = 0; i < uia.length; i++) {
                    if (localName.equals(uia[i].getLocalName())) {
                        return uia[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method is used exclusively to get the pass through attributes!
     * Namespaces http://xmlns.jcp.org/jsf/passthrough and http://xmlns.jcp.org/jsf/passthrough
     * Get all TagAttributesImpl for the passed namespace
     * 
     * @param namespace
     *            namespace to search
     * @return a non-null array of TagAttributesImpl
     */
    @Override
    public TagAttribute[] getAll(String namespace) {
        int idx = 0;
        if (namespace == null) {
            idx = Arrays.binarySearch(this.ns, "");
        } else {
            idx = Arrays.binarySearch(this.ns, namespace);
        }
        if (idx >= 0) {
            return (TagAttribute[]) this.nsattrs.get(idx);
        }
        return EMPTY;
    }

    /**
     * A list of Namespaces found in this set
     * 
     * @return a list of Namespaces found in this set
     */
    @Override
    public String[] getNamespaces() {
        return this.ns;
    }

    @Override
    public Tag getTag() {
        return this.tag;
    }
    
    public void setTag(Tag tag) {
        this.tag = tag;
        for (TagAttribute cur : attrs) {
            cur.setTag(tag);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.attrs.length; i++) {
            sb.append(this.attrs[i]);
            sb.append(' ');
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
