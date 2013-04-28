package jittac.jdt;

import static java.lang.System.arraycopy;
import jittac.jdt.builder.JavaImplementationModelBuilder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class JavaACNature implements IProjectNature {
    /** Unique ID of this project nature.  */
    public static final String ID = "jittac.jdt.javaacnature";
    private IProject project;

    /**
     * Adds the {@link JavaImplementationModelBuilder} to the build specification of the project.
     */
    @Override
    public void configure() throws CoreException {
        IProjectDescription desc = project.getDescription();
        ICommand[] commands = desc.getBuildSpec();

        // Do nothing If the builder is already included in the build specification!
        for (int i = 0; i < commands.length; ++i) {
            if (JavaImplementationModelBuilder.ID.equals(commands[i].getBuilderName())) {
                return;
            }
        }

        // Create new build specification with the new builder.
        ICommand[] newCommands = new ICommand[commands.length + 1];
        arraycopy(commands, 0, newCommands, 0, commands.length);
        ICommand command = desc.newCommand();
        command.setBuilderName(JavaImplementationModelBuilder.ID);
        newCommands[newCommands.length - 1] = command;

        desc.setBuildSpec(newCommands);
        project.setDescription(desc, null);
    }

    /**
     * Removes the {@link JavaImplementationModelBuilder} to the build specification of the project.
     */
    @Override
    public void deconfigure() throws CoreException {
        IProjectDescription description = getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();

        // Remove the Java IM builder from the build spec 
        // (assumes that only one such builder can be present in the build spec).
        for (int i = 0; i < commands.length; ++i) {
            if (JavaImplementationModelBuilder.ID.equals(commands[i].getBuilderName())) {
                ICommand[] newCommands = new ICommand[commands.length - 1];
                arraycopy(commands, 0, newCommands, 0, i);
                arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                
                description.setBuildSpec(newCommands);
                project.setDescription(description, null);
                break;
            }
        }
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }
}
