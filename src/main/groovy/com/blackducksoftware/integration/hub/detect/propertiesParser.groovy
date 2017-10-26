import com.blackducksoftware.integration.hub.detect.properties.DetectPropertyData
import com.google.gson.Gson
import com.google.gson.GsonBuilder

Gson gson = new GsonBuilder().setPrettyPrinting().create()

def propertyOrderedKeys = []
def propertyNameToData = [:]
def keyToData = [:]

def configurationOrderedNames = []

def detectPropertiesFile = new File('/Users/ekerwin/Documents/source/integration/hub-detect/src/main/groovy/com/blackducksoftware/integration/hub/detect/DetectProperties.groovy')
def propertiesLines = detectPropertiesFile.readLines()
propertiesLines.eachWithIndex { line, index ->
    def detectPropertyData = new DetectPropertyData()
    if (line.trim().startsWith('@ValueDescription')) {
        detectPropertyData.propertyOrder = propertyOrderedKeys.size()
        detectPropertyData.description = extractInnerPiece(line, 'description="', '"')
        if (line.contains('defaultValue="')) {
            detectPropertyData.defaultValue = extractInnerPiece(line, 'defaultValue="', '"')
        }
        detectPropertyData.group = extractInnerPiece(line, 'group=DetectProperties.GROUP_', ')')

        String valueLine = propertiesLines[index + 1].trim()
        detectPropertyData.propertyKey = extractInnerPiece(valueLine, '{', '}')

        String typeLine = propertiesLines[index + 2].trim()
        detectPropertyData.propertyType = typeLine.substring(0, typeLine.indexOf(' '))
        detectPropertyData.detectPropertyName = typeLine.substring(typeLine.indexOf(' ') + 1)

        propertyOrderedKeys.add(detectPropertyData.propertyKey)
        keyToData[detectPropertyData.propertyKey] = detectPropertyData
        propertyNameToData[detectPropertyData.detectPropertyName] = detectPropertyData
    }
}

def shouldProcess = false
def detectConfigurationName = ''
def detectConfigurationFile = new File('/Users/ekerwin/Documents/source/integration/hub-detect/src/main/groovy/com/blackducksoftware/integration/hub/detect/DetectConfiguration.groovy')
def configurationLines = detectConfigurationFile.readLines()
configurationLines.eachWithIndex { line, index ->
    if (line.trim().startsWith('//AUTO-GENERATE PROPERTIES START MARKER')) {
        shouldProcess = true
    } else if (line.trim().startsWith('//AUTO-GENERATE PROPERTIES END MARKER')) {
        shouldProcess = false
    }

    if (shouldProcess) {
        if (detectConfigurationName) {
            String javaPrefix = ''
            String javaSuffix = ''
            if (line.contains('(detectProperties.')) {
                javaPrefix = extractInnerPiece(line, 'return ', 'detectProperties.')
            }
            String propertyName = (line =~ /detectProperties\.([A-Za-z]+).*$/)[0][1]
            String whatIsLeft = line.replace('return ', '')
            whatIsLeft = whatIsLeft.replace('detectProperties.', '')
            whatIsLeft = whatIsLeft.replace(propertyName, '')
            if (javaPrefix || whatIsLeft) {
                javaSuffix = line.substring(line.indexOf(propertyName) + propertyName.length())
            }

            def data = propertyNameToData[propertyName]
            if (null == data) {
                throw new Exception("what the hell ${propertyName}")
            }
            if (javaPrefix) {
                data.javaCodePrefix = javaPrefix
            }
            if (javaSuffix) {
                data.javaCodeSuffix = javaSuffix
            }
            data.detectConfigurationName = detectConfigurationName
            data.configurationOrder = configurationOrderedNames.size()

            configurationOrderedNames.add(detectConfigurationName)
            detectConfigurationName = ''
        } else {
            if (line.trim().startsWith('public ') && line.contains(' get')) {
                detectConfigurationName = extractInnerPiece(line, ' get', '(')
            }
        }
    }
}

new File('/Users/ekerwin/Documents/source/integration/hub-detect/src/main/resources/application_properties.json') << gson.toJson(keyToData.values())

String extractInnerPiece(String full, String leftToken, String rightToken) {
    int left = full.indexOf(leftToken) + leftToken.length()
    int right = full.indexOf(rightToken, left)
    full.substring(left, right)
}
