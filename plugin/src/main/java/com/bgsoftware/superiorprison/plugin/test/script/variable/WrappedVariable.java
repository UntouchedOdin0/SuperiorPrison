package com.bgsoftware.superiorprison.plugin.test.script.variable;

public class WrappedVariable<T> implements Variable<T>{

    private Variable<T> replaceableVariable;
    public WrappedVariable(Variable<T> variable) {
        this.replaceableVariable = variable;
    }

    @Override
    public Class<T> getType() {
        return replaceableVariable.getType();
    }

    @Override
    public T get(GlobalVariableMap globalVariableMap) {
        return replaceableVariable.get(globalVariableMap);
    }

    public synchronized void replace(Variable<T> variable) {
        this.replaceableVariable = variable;
    }
}