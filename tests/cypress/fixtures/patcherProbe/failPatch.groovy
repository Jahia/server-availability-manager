import org.jahia.tools.patches.Patcher

def patchesFolder = Patcher.getInstance().getPatchesFolder()

def process = """mv ${patchesFolder}/groovy/7.3.5.0-01-clearLock-permission.groovy.skipped ${patchesFolder}/groovy/7.3.5.0-01-clearLock-permission.groovy.failed""".execute()

println "${process.text}"
