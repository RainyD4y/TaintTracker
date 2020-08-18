package config;

import soot.Scene;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;

import java.util.LinkedList;
import java.util.List;

public class ConfigForAndroidMultipleDex implements IInfoflowConfig {
    @Override
    public void setSootOptions(Options options, InfoflowConfiguration config) {
            List<String> excludeList = new LinkedList<>();
            excludeList.add("java.*");
            excludeList.add("sun.*");
            excludeList.add("android.*");
            excludeList.add("org.apache.*");
            excludeList.add("org.eclipse.*");
            excludeList.add("soot.*");
            excludeList.add("javax.*");

            Options.v().set_exclude(excludeList);
            Options.v().set_whole_program(true);
            Options.v().set_no_bodies_for_excluded(true);
            Options.v().set_process_multiple_dex(true);
            options.set_output_format(Options.output_format_none);
            config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK);
            config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
    }
}
