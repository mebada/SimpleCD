package org.icetank.simpleCD;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Hello world!
 *
 */
public class App {
	final static Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) {

		logger.info("◠‿◠  Welecome to SimpleCD Engine v0.1  ◠‿◠");
		logger.info("\n\n");
		if (args.length < 1) {
			logger.info("( ͝° ͜ʖ͡°) I see no app.yml, you may create one and pass to this jar");
			logger.info("         Just like below ... ");
			logger.info("\n\n");

			logger.info("scanCode:yes " + "\n" + "SourceControlType: git" + "\n"
					+ "SourceControlURL:  https://github.ibm.com/mohap/MOHAPPortal" + "\n"
					+ "SourceControlBranch: develop" + "\n" + "SourceControlUser: mebada" + "\n"
					+ "SourceControlPassword: 123$@SDlkhsdfsf" + "\n" + "tagCode:yes" + "\n" + "Build:yes " + "\n"
					+ "BuildCommand: mvn clean install" + "\n" + "deoploy:yes" + "\n"
					+ "artifactSource: /home/mebada/workspace/mohap/MOHAPPortal/target/ear.ear" + "\n"
					+ "targetMachingAlias: dev" + "\n" + "targetMachingIP: 10.12.11.1" + "\n"
					+ "targetMachingUser: ibmact" + "\n" + "targetMachingPassword: ibmact" + "\n"
					+ "targetMachingDistDir: /pportal/dist/" + "\n"
					+ "deployCommand: /home/mebada/workspace/mohap/MOHAPPortal/target/deploy.sh" + "\n"
					+ "cdLogs: /home/mebada/logs/" + "\n");

			logger.info("\n\n");
			logger.info("◔ ⌣ ◔  Sadly closing, see you soon!");
			System.exit(1);
		}
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		try {

			logger.info("😂  I am reading your pipeline.yaml !");

			Pipeline pipeline = mapper.readValue(new File(args[0]), Pipeline.class);
			logger.info("😂  Your pipeline.yaml looks ok!");

			String wsLoc = pipeline.getSourceControl().get("workspace");

			if (Paths.get(wsLoc).toFile().isDirectory()) {
				FileUtils.deleteDirectory(new File(wsLoc));
				logger.info("😂  I am cleaning " + wsLoc);

			}

			File ws = new File(wsLoc);

			logger.info("😂  I am creating " + wsLoc);

			UsernamePasswordCredentialsProvider cred = new UsernamePasswordCredentialsProvider(
					pipeline.getSourceControl().get("user"), pipeline.getSourceControl().get("password"));

			logger.info("😂  I am cloning the code from  " + pipeline.getSourceControl().get("url") + ".");

			Git g = Git.cloneRepository().setURI(pipeline.getSourceControl().get("url"))
					.setBranch(pipeline.getSourceControl().get("branch")).setDirectory(ws).setCredentialsProvider(cred)
					.call();
			logger.info("😂  Clone is done");

			// Thread.sleep(60);

			// run the Unix "ps -ef" command
			String[] buildCommands = pipeline.getBuild().get("BuildCommand").split("&&");
			for (String buildCommand : buildCommands) {
				Process p = Runtime.getRuntime().exec(buildCommand);

				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				// read the output from the command

				System.out.println("Here is the standard output of the command:\n");
				String s;
				while ((s = stdInput.readLine()) != null) {
					System.out.println(s);

					if (pipeline.getBuild().get("tagCode").equals("yes")) {
						String date = new SimpleDateFormat("yyyyMMdd-hhmm").format(new Date());
						g.tag().setName(date).call();
						g.push().setPushTags().setCredentialsProvider(cred).call();
					}

				}

				// read any errors from the attempted command

				System.out.println("Here is the standard error of the command (if any):\n");
				while ((s = stdError.readLine()) != null) {
					System.out.println(s);
				}
			}

			String artifactLocation = pipeline.getDeploy().get("artifactLocation");
			String user = pipeline.getDeploy().get("user");
			String password = pipeline.getDeploy().get("password");
			String host = pipeline.getDeploy().get("host");
			String remoteWorkspace = pipeline.getDeploy().get("remoteWorkspace");
			String[] copyto = { artifactLocation,
					user+"@"+host+":"+remoteWorkspace, password };
			ScpTo.main(copyto);
	
		      String command = pipeline.getDeploy().get("deployCommand");
		      
			String deploy[] = {host,user,password,command};
			
		     String userName = user;
		     String connectionIP = host;
		     SSHManager instance = new SSHManager(userName, password, connectionIP, "");
		     String errorMessage = instance.connect();

		     logger.info(errorMessage);

		     String result = instance.sendCommand("ls");
		     logger.info(result);
		     result = instance.sendCommand(command);
		     logger.info(result);
		     // close only after all commands are sent
		     instance.close();
			

			logger.info("😂  I am deleting the workspace");
			// FileUtils.deleteDirectory(ws);
			logger.info("😂  workdspace is deleted");

		} catch (Exception e) {
			logger.info("(⌣̩̩́_⌣̩̩̀) something wrong happened, more details below.");
			e.printStackTrace();
			System.exit(1);

		}

	}
}
