import org.jahia.tools.patches.Patcher

def patchesFolder = Patcher.getInstance().getPatchesFolder()

def process = """mv ${patchesFolder}/groovy/8.1.0.0-01-removeModules.jcrStoreProviderStarted.groovy.failed ${patchesFolder}/groovy/8.1.0.0-01-removeModules.jcrStoreProviderStarted.groovy.skipped""".execute()

println "${process.text}"
