package io.klib.gogo.scriptstarter;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(immediate = true)
public class AutoScriptExecution {

    private static final String GOGO_SCRIPTS = "gogoScripts";
    private final Logger        logger       = LoggerFactory.getLogger(this.getClass());
    private String[]            args;
    private CommandProcessor    cmdproc;
    private final static String gogoScope    = "gogo";
    private final static String gogoCommand  = "source";

    @Reference(target = "(launcher.arguments=*)")
    void args(final Object object, final Map<String,Object> map) {
        args = (String[]) map.get("launcher.arguments");
    }

    @Reference
    void setCommandSession(final CommandProcessor cp) {
        this.cmdproc = cp;
    }

    @Activate
    public void activate() {
        String command = "";
        boolean foundScriptArg = false;
        if (args.length > 0) {
            for (String arg: args) {
                if (arg.equalsIgnoreCase(GOGO_SCRIPTS)) {
                    foundScriptArg = true;
                }
                else {
                    if (foundScriptArg) {
                        if (isGogoCommandAvailable(gogoScope, gogoCommand)) {
                            CommandSession cs = cmdproc.createSession(null, System.out, System.err);
                            try {
                                long startTime = System.currentTimeMillis();
                                command = gogoScope + ":" + gogoCommand + " " + arg;
                                cs.execute(command);
                                cs.close();
                                String msg = MessageFormat.format("execution took <{0}> ms for gogo command <{1}> ",
                                                                  System.currentTimeMillis() - startTime,
                                                                  arg);
                                logger.info(msg);
                            }
                            catch (IllegalArgumentException iae) {
                                logger.error("unable to execute : " + command, iae);
                                printGogoHelp(cs, gogoScope, gogoCommand);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                cs.close();
                            }

                        }
                    }
                }
            }
        }
    }

    private void printGogoHelp(final CommandSession cs, final String argScope, final String argFunction) {
        String userCmd = MessageFormat.format("{0}:{1}", argScope, argFunction);
        try {
            System.out.println(MessageFormat.format("The specified cmd {0} has no valid syntax. Following signatures are supported.",
                                                    userCmd));
            cs.execute("help " + userCmd);
        }
        catch (Exception e) {
            // should not happen
        }
    }

    private boolean isGogoCommandAvailable(final String gogoScope, final String gogoCommand) {
        boolean rv = false;
        BundleContext bc = FrameworkUtil.getBundle(AutoScriptExecution.class).getBundleContext();
        @SuppressWarnings("rawtypes")
        ServiceReference[] refs = null;

        String filter = MessageFormat.format("(&({0}={1})({2}={3}))",
                                             CommandProcessor.COMMAND_SCOPE,
                                             gogoScope,
                                             CommandProcessor.COMMAND_FUNCTION,
                                             gogoCommand);
        try {
            refs = bc.getAllServiceReferences(null, filter);
            if (refs.length > 0) {
                rv = true;
            }
        }
        catch (InvalidSyntaxException e) {
            String msg = MessageFormat.format("No OSGi command found for {0}:{1}", gogoScope, gogoCommand);
            logger.info(msg);
        }
        return rv;
    }

}