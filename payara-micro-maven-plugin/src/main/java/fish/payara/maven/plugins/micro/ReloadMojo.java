/*
 *
 * Copyright (c) 2020-2021 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.maven.plugins.micro;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Reload mojo that reloads exploded web application in running payara-micro
 * instance.
 *
 * @author Gaurav Gupta
 */
@Mojo(name = "reload")
public class ReloadMojo extends BasePayaraMojo {

    private static final String RELOAD_FILE = ".reload";
    
    @Parameter(property = "hotDeploy")
    private boolean hotDeploy;

    @Parameter(property = "sourcesChanged")
    private String sourcesChanged;

    @Parameter(property = "metadataChanged")
    private boolean metadataChanged;
    
    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Reload mojo execution is skipped");
            return;
        }
        Build build = mavenProject.getBuild();
        String finalName = StringUtils.isNotEmpty(build.getFinalName())
                ? build.getFinalName()
                : mavenProject.getArtifact().getArtifactId() + '-' + mavenProject.getVersion();
        String explodedDirPath = build.getDirectory() + File.separator + finalName;

        File explodedDir = new File(explodedDirPath);
        if (!explodedDir.exists()) {
            throw new MojoExecutionException(String.format("explodedDir[%s] not found", explodedDirPath));
        }
        File reloadFile = new File(explodedDir, RELOAD_FILE);
        if (hotDeploy) {
            Properties props = new Properties();
            props.setProperty("hotdeploy", "true");
            if (metadataChanged) {
                props.setProperty("metadatachanged", "true");
            }
            if (sourcesChanged != null && !sourcesChanged.isEmpty()) {
                props.setProperty("sourceschanged", sourcesChanged);
            }
            try (FileOutputStream outputStrem = new FileOutputStream(reloadFile)) {
                props.store(outputStrem, null);
            } catch (Exception ex) {
                throw new MojoExecutionException("Unable to save .reload file " + ex.toString());
            }
        } else if (reloadFile.exists()) {
            try ( PrintWriter pw = new PrintWriter(reloadFile)) {
            } catch (FileNotFoundException ex) {
                throw new MojoExecutionException("Unable to find .reload file " + ex.toString());
            }
            reloadFile.setLastModified(System.currentTimeMillis());
        } else {
            try {
                reloadFile.createNewFile();
            } catch (IOException ex) {
                throw new MojoExecutionException("Unable to create .reload file " + ex.toString());
            }
        }

    }

}
