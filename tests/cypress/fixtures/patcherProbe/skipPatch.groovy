import org.jahia.tools.patches.Patcher

def patchesFolder = Patcher.getInstance().getPatchesFolder()

def process = """mv ${patchesFolder}/groovy/7.3.5.0-01-clearLock-permission.groovy.failed ${patchesFolder}/groovy/7.3.5.0-01-clearLock-permission.groovy.skipped""".execute()

println "${process.text}"
