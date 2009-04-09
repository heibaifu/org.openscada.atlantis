package org.openscada.da.server.exec2.command;

public class ExecutionResult
{
    /**
     * The exit value of the command call, <code>null</code> if the application is still running
     */
    private Integer exitValue = null;

    /**
     * The output from the process
     */
    private String output = "";

    /**
     * The error output from the process
     */
    private String errorOutput = "";

    /**
     * An exception that was caused by the execution 
     */
    private Throwable executionError = null;

    /**
     * The time the application took for completing, <code>null</code> if the process is still running
     */
    private Long runtime;

    public Integer getExitValue ()
    {
        return this.exitValue;
    }

    public void setExitValue ( final Integer exitValue )
    {
        this.exitValue = exitValue;
    }

    public String getOutput ()
    {
        return this.output;
    }

    public void setOutput ( final String output )
    {
        this.output = output;
    }

    public String getErrorOutput ()
    {
        return this.errorOutput;
    }

    public void setErrorOutput ( final String errorOutput )
    {
        this.errorOutput = errorOutput;
    }

    public Throwable getExecutionError ()
    {
        return this.executionError;
    }

    public void setExecutionError ( final Throwable executionError )
    {
        this.executionError = executionError;
    }

    public Long getRuntime ()
    {
        return this.runtime;
    }

    public void setRuntime ( final Long runtime )
    {
        this.runtime = runtime;
    }

    @Override
    public String toString ()
    {
        return this.output;
    }
}
