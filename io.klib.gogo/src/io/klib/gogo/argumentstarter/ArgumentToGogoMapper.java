package io.klib.gogo.argumentstarter;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(immediate = true)
public class ArgumentToGogoMapper {

    private String[]         args;
    private CommandProcessor cmdproc;

    @Reference(target = "(launcher.arguments=*)")
    void args(final Object object, final Map<String,Object> map) {
        args = (String[]) map.get("launcher.arguments");
    }

    @Reference
    void setCommandSession(final CommandProcessor cp) {
        this.cmdproc = cp;
    }

    @Activate
    private void activate() {
        long startTime = System.currentTimeMillis();
        // we process only if program arguments are available
        if (args.length > 0) {
            String[] gogoCommands = args[0].split(":");
            String argScope = gogoCommands[0];
            String argFunction = gogoCommands[1];

            BundleContext bc = FrameworkUtil.getBundle(ArgumentToGogoMapper.class).getBundleContext();
            @SuppressWarnings("rawtypes")
            ServiceReference[] refs = null;

            String filter = MessageFormat.format("(&({0}={1})({2}={3}))",
                                                 CommandProcessor.COMMAND_SCOPE,
                                                 argScope,
                                                 CommandProcessor.COMMAND_FUNCTION,
                                                 argFunction);
            try {
                refs = bc.getAllServiceReferences(null, filter);
            }
            catch (InvalidSyntaxException e) {
                // this should not happen
            }

            if (refs.length == 0) {
                String msg = MessageFormat.format("No OSGi command found for {0}:{1}", argScope, argFunction);
                System.out.println(msg);
            }
            else {
                String joinedGogoShellCmd = Arrays.asList(args).stream().map(i -> i.toString()).collect(Collectors.joining(" ")); 
                CommandSession cs = cmdproc.createSession(null, System.out, System.err);
                try {
                    cs.execute(joinedGogoShellCmd);
                    String msg = MessageFormat.format("execution took <{0}> ms for gogo command <{1}> ",
                                                      System.currentTimeMillis() - startTime,
                                                      joinedGogoShellCmd);
                    System.out.println(msg);
                }
                catch (IllegalArgumentException iae) {
                    printGogoHelp(cs, argScope, argFunction);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // shutting down the framework
            try {
                bc.getBundle(0).stop();
                Thread.sleep(1000);
            }
            catch (BundleException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                System.exit(0);
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
}
