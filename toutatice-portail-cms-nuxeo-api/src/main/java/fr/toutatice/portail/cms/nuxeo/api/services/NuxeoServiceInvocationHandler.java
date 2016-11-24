package fr.toutatice.portail.cms.nuxeo.api.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

/**
 * Nuxeo service invocation handler.
 *
 * @author Cédric Krommenhoek
 * @see InvocationHandler
 */
public class NuxeoServiceInvocationHandler implements InvocationHandler {

    /** Instance. */
    private Object instance;


    /**
     * Constructor.
     */
    public NuxeoServiceInvocationHandler() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Save context class loader
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        // Set proxy class loader
        Thread.currentThread().setContextClassLoader(this.instance.getClass().getClassLoader());


        // Result
        Object result = null;
        try {
            // Method invocation
            result = method.invoke(this.instance, args);
        } catch(Exception e){
            ReflectionUtils.handleReflectionException(e);
        } finally {
            // Restore context class loader
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        return result;
    }


    /**
     * Getter for instance.
     *
     * @return the instance
     */
    public Object getInstance() {
        return this.instance;
    }

    /**
     * Setter for instance.
     *
     * @param instance the instance to set
     */
    public void setInstance(Object instance) {
        this.instance = instance;
    }

}
