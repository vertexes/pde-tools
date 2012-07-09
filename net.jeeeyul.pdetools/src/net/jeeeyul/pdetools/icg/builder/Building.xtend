package net.jeeeyul.pdetools.icg.builder

import net.jeeeyul.pdetools.icg.ICGConfiguration
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.IProgressMonitor
import net.jeeeyul.pdetools.icg.builder.model.PaletteModelGenerator

class Building {
	int kind
	extension IncrementalProjectBuilder builder
	ICGConfiguration _config

	new(IncrementalProjectBuilder builder, int kind) {
		this.builder = builder
		this.kind = kind
	}

	def IProject[ ] build(IProgressMonitor monitor) {
		var pmg = new PaletteModelGenerator(config)
		var paletteModel = pmg.generatePalette(config.monitoringFolder)
		var generator = new ImageCosntantGenerator()
		generator.config = config
		generator.rootPalette = paletteModel
		println(generator.generate); return emptyList
	}

	def ICGConfiguration getConfig() {
		if(_config == null) {
			_config = new ICGConfiguration(project)
		}
		return _config
	}

	def hasToBuild() {
		if(kind == IncrementalProjectBuilder::CLEAN_BUILD || kind == IncrementalProjectBuilder::FULL_BUILD) {
			return true;
		}
		var projectDelta = project.delta
		if(projectDelta == null) {
			return false;
		}
		var monitoringFolder = config.monitoringFolder
		if(!monitoringFolder.exists) {
			return false;
		}
		var monitoredDelta = projectDelta.findMember(config.monitoringFolder.fullPath)
		if(monitoredDelta == null) {
			return false;
		}
		return true;
	}
}