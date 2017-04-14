/****************************************************************************
 *
 * Copyright (C) 2004. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 24-August-2004
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.midiki.logic;

import java.util.*;

/**
 * A baseline implementation of the Bindings interface.
 * This might be moved to package org.mitre.midiki.impl.mitre
 * in a future release, in order to better support the concept
 * of the Midiki core as primarily consisting of interfaces
 * to pluggable implementations.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see HashMap
 * @see Bindings
 */
public class BindingsImpl extends HashMap implements Bindings
{
    public class BindingScope
    {
        HashMap deltas;
        LinkedList alternatives;
        Iterator btr;
        public BindingScope()
        {
            deltas = null;
            alternatives = null;
            btr = null;
        }
        /**
         * Resets all variable bindings for the current scope,
         * defining the start of a new alternative solution.
         *
         */
        public void reset()
        {
            if (deltas == null) return;
            if (deltas.size() == 0) return;
            if (alternatives == null) {
                alternatives = new LinkedList();
            }
            alternatives.add(deltas);
            deltas = new HashMap();
        }
        public void register(Object key, Object val)
        {
            if (deltas == null) {
                deltas = new HashMap();
                deltas.put(key, val);
            } else if (deltas.get(key) == null) {
                deltas.put(key, val);
            } else {
                //System.out.println("refused to register("+key+","+val+")");
            }
        }
        public Iterator changes()
        {
            if (deltas == null) return null;
            return deltas.keySet().iterator();
        }
        public Object value(Object key)
        {
            return deltas.get(key);
        }
        public void close()
        {
            if (deltas != null) deltas.clear();
            if (alternatives != null) {
                Iterator it = alternatives.iterator();
                while (it.hasNext()) {
                    ((HashMap)it.next()).clear();
                }
                alternatives.clear();
            }
            deltas = null;
            alternatives = null;
            btr = null;
        }
        public String toString()
        {
            String dta = ((deltas!=null) ? deltas.toString() : "null");
            String alt = ((alternatives!=null) ? alternatives.toString() : "null");
            return dta+":"+alt;
        }
    }

    protected BindingScope currentScope;
    protected Stack scopes;

    public BindingsImpl()
    {
        super();
        scopes = new Stack();
        enterScope();
    }
    public BindingsImpl(int initialCapacity)
    {
        super(initialCapacity);
        scopes = new Stack();
        enterScope();
    }
    public BindingsImpl(int initialCapacity, float loadFactor)
    {
        super(initialCapacity,loadFactor);
        scopes = new Stack();
        enterScope();
    }
    public BindingsImpl(Map m)
    {
        super(m);
        scopes = new Stack();
        enterScope();
    }

    /**
     * Describe <code>get</code> method here.
     *
     * @param variable a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object get(String variable)
    {
        return super.get(variable);
    }
    /**
     * Describe <code>get</code> method here.
     *
     * @param variable a <code>Variable</code> value
     * @return an <code>Object</code> value
     */
    public Object get(Variable variable)
    {
        return get(variable.name());
    }
    private void removeChanges()
    {
        Iterator it = currentScope.changes();
        if (it == null) return;
        while (it.hasNext()) {
            remove(it.next());
        }
    }
    private void applyChanges()
    {
        Iterator it = currentScope.changes();
        if (it == null) return;
        while (it.hasNext()) {
            Object key = it.next();
            put(key, currentScope.value(key));
        }
    }
    public void clearCurrentScope()
    {
        removeChanges();
        if (currentScope != null) {
            if (currentScope.deltas != null) {
                currentScope.deltas.clear();
            }
        }
    }
    /**
     * Unbinds any variables bound by the current context and attempts to
     * rebind them to the next alternative. Fails if there are no remaining
     * alternatives.
     *
     * @return <code>true</code> if another alternate binding is available
     */
    public boolean backtrack()
    {
        //System.out.println("backtrack(): currentScope == "+currentScope);
        if (currentScope == null) {
            //System.out.println("backtrack: currentScope == null");
            return false;
        }
        if (currentScope.btr == null) {
            //System.out.println("backtrack: currentScope.btr == null");
            return false;
        }
        //System.out.println(currentScope.alternatives);
        if (currentScope.btr.hasNext()) {
            removeChanges();
            currentScope.deltas = (HashMap)currentScope.btr.next();
            applyChanges();
            return true;
        } else {
            //System.out.println("backtrack: currentScope.btr.hasNext() == false");
        }
        return false;
    }
    /**
     * Marks the start of a new binding scope.
     *
     * @return a <code>boolean</code> value
     */
    public boolean enterScope()
    {
        // create a new local scope
        currentScope = new BindingScope();
        // push current scope on stack
        scopes.push(currentScope);
        return true;
    }
    /**
     * Copies the specified scope onto the current scope
     * and initializes the backtracking iterator.
     * Assumes this routine is called immediately after
     * returning from a query, that enterScope was called
     * before executing the query, and that exitScope
     * will be called after we're all done.
     *
     * @return a <code>boolean</code> value
     */
    public boolean prepareScope(BindingScope bs)
    {
        //System.out.println("prepareScope("+bs+"), currentScope == "+currentScope);
        if (bs == null) return false;
        currentScope.close();
        currentScope.deltas = bs.deltas;
        currentScope.alternatives = bs.alternatives;
        if (currentScope.alternatives == null) {
            //System.out.println("currentScope.alternatives == null");
            return false;
        }
        currentScope.btr = currentScope.alternatives.iterator();
        if (currentScope.btr.hasNext()) {
            //System.out.println("currentScope.btr == "+currentScope.btr);
            currentScope.deltas = (HashMap)currentScope.btr.next();
            applyChanges();
            return true;
        } else {
            return false;
        }
    }
    public boolean initializeBacktracking()
    {
        //System.out.println(scopes);
        currentScope = (BindingScope)scopes.peek();
        BindingScope bs = currentScope;
        popScope();
        enterScope();
        prepareScope(bs);
        return true;
    }
    /**
     * Marks the end of a binding scope. Any variables bound since the
     * last call to <code>enterScope</code> will be unbound, and the
     * scope stack will be popped.
     *
     * @return a <code>boolean</code> value
     */
    public boolean exitScope()
    {
        if (currentScope == null) return false;
        // remove all bindings since we entered this scope
        removeChanges();
        currentScope.close();
        scopes.pop();
        currentScope = (BindingScope)scopes.peek();
        return true;
    }
    /**
     * Exits the current scope, but does not destroy the contents.
     * Used to keep the scope around for transfer back to the agent.
     *
     * @return a <code>boolean</code> value
     */
    public boolean popScope()
    {
        if (currentScope == null) return false;
        // remove all bindings since we entered this scope
        removeChanges();
        scopes.pop();
        return true;
    }
    /**
     * Resets all variable bindings for the current scope,
     * defining the start of a new alternative solution.
     *
     */
    public void reset()
    {
        if (currentScope == null) return;
        removeChanges();
        currentScope.reset();
    }
    
    /**
     * Overrides the standard HashMap put method, storing the key in
     * <code>deltas</code> if the key is not yet stored in the map.
     * It does not try to handle modifications, just additions;
     * a modification shouldn't be attempted as a binding by the Unifier.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object put(Object key,
                      Object value)
    {
        if (key instanceof Variable) {
            key = ((Variable)key).name();
        }
        if (get(key) == null) {
            if (currentScope == null) {
                currentScope = new BindingScope();
            }
            currentScope.register(key, value);
            return super.put(key, value);
        } else {
            return null;
        }
    }

    /**
     * Removes the binding for this variable if it is present.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object remove(Object key)
    {
        if (key instanceof Variable) {
            key = ((Variable)key).name();
        }
        return super.remove(key);
    }

    protected Collection marshalScope(BindingScope bs)
    {
        LinkedList result = new LinkedList();
        if (bs != null) {
            result.add(bs.deltas);
            result.add(bs.alternatives);
        }
        return result;
    }

    public Collection marshalLatest()
    {
        //System.out.println("marshalLatest: scopes before == "+scopes);
        Collection c = marshalScope(currentScope);
        //System.out.println("marshalLatest: result after == "+c);
        return c;
    }

    public Collection marshalAll()
    {
        //System.out.println("marshalAll: scopes before == "+scopes);
        LinkedList result = new LinkedList();
        Iterator sit = scopes.iterator();
        while (sit.hasNext()) {
            result.add(marshalScope((BindingScope)sit.next()));
        }
        //System.out.println("marshalAll: result after == "+result);
        return result;
    }

    protected void unmarshalScope(Collection c)
    {
        if (c==null) {
            //System.out.println("bindings.unmarshalScope(null)");
            return;
        }
        LinkedList result = (LinkedList)c;
        if (result.isEmpty()) {
            //System.out.println("bindings.unmarshalScope([])");
            return;
        }
        Object dta_map=null;
        try {
            dta_map = result.get(0);
            if (dta_map != null) {
                HashMap dtas = (HashMap)dta_map;
                Iterator dit = dtas.entrySet().iterator();
                while (dit.hasNext()) {
                    Map.Entry me = (Map.Entry)dit.next();
                    put(me.getKey(), me.getValue());
                }
            } else {
                // no deltas
            }
            currentScope.alternatives = (LinkedList)result.get(1);
        } catch (ClassCastException cce) {
            System.out.println("dta_map class is "+dta_map.getClass().getName());
            System.out.println("dta_map is "+dta_map);
            cce.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            // elements expected in list were not there
        }
        //System.out.println("bindings.unmarshalScope() == "+this);
    }

    public void unmarshalLatest(Collection c)
    {
        //System.out.println("unmarshalLatest: incoming collection == "+c);
        if (currentScope == null) enterScope();
        unmarshalScope(c);
        //System.out.println("unmarshalLatest: scopes after == "+scopes);
    }

    public void unmarshalAll(Collection c)
    {
        //System.out.println("unmarshalAll: incoming collection == "+c);
        boolean first=true;  // a fresh set of bindings has an open scope
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Collection cc = (Collection)it.next();
            if (!first) enterScope();
            unmarshalScope(cc);
            first = false;
        }
        //System.out.println("unmarshalAll: scopes after == "+scopes);
        //System.out.println("bindings.unmarshalAll() == "+this);
    }
}
