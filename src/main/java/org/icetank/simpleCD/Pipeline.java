package org.icetank.simpleCD;

import java.util.Map;

public class Pipeline {
    private String name;

    private Map<String, String> sourceControl;
    private Map<String, String> build;
    private Map<String, String> deploy;


    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name = name;

    }

	public Map<String, String> getSourceControl() {
		return sourceControl;
	}

	public void setSourceControl(Map<String, String> sourceControl) {
		this.sourceControl = sourceControl;
	}

	public Map<String, String> getBuild() {
		return build;
	}

	public void setBuild(Map<String, String> build) {
		this.build = build;
	}

	public Map<String, String> getDeploy() {
		return deploy;
	}

	public void setDeploy(Map<String, String> delpoy) {
		this.deploy = delpoy;
	}


}
