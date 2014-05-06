/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.services;

/**
 * Injection data java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class InjectionData {

    /** Nuxeo parent document path. */
    private String parentPath;
    /** Nuxeo workspace path. */
    private String workspacePath;
    /** Count. */
    private int count;
    /** Notes count. */
    private int notesCount;
    /** Depth. */
    private int depth;
    /** Probabilities. */
    private float[] probabilities;


    /**
     * Default constructor.
     */
    public InjectionData() {
        super();
    }


    /**
     * Getter for parentPath.
     *
     * @return the parentPath
     */
    public String getParentPath() {
        return this.parentPath;
    }

    /**
     * Setter for parentPath.
     *
     * @param parentPath the parentPath to set
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * Getter for workspacePath.
     *
     * @return the workspacePath
     */
    public String getWorkspacePath() {
        return this.workspacePath;
    }

    /**
     * Setter for workspacePath.
     *
     * @param workspacePath the workspacePath to set
     */
    public void setWorkspacePath(String workspacePath) {
        this.workspacePath = workspacePath;
    }

    /**
     * Getter for count.
     *
     * @return the count
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Setter for count.
     *
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Getter for notesCount.
     * 
     * @return the notesCount
     */
    public int getNotesCount() {
        return this.notesCount;
    }

    /**
     * Setter for notesCount.
     * 
     * @param notesCount the notesCount to set
     */
    public void setNotesCount(int notesCount) {
        this.notesCount = notesCount;
    }

    /**
     * Getter for depth.
     * 
     * @return the depth
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Setter for depth.
     *
     * @param depth the depth to set
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Getter for probabilities.
     *
     * @return the probabilities
     */
    public float[] getProbabilities() {
        return this.probabilities;
    }

    /**
     * Setter for probabilities.
     *
     * @param probabilities the probabilities to set
     */
    public void setProbabilities(float[] probabilities) {
        this.probabilities = probabilities;
    }

}
