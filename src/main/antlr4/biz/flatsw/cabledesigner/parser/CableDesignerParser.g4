parser grammar CableDesignerParser;

options {   tokenVocab = CableDesignerLexer; }


document: harness+;

harness:
          connectorDefinition
        | wireDefinition
        | include
        | device
        | connector
        | deviceAttachment
        | path
        | signal
        | documentProperty
        ;

documentProperty: KEYWORD_DOCUMENT name=TEXT KEYWORD_IS value=TEXT;

// file inclusion
include: KEYWORD_INCLUDE file=TEXT;

// wire type definition
wireDefinition: KEYWORD_DEFINE KEYWORD_WIRE
            wireCrossSection_rule wireInsulation_rule
            currentRating_rule
            wireDefinitionColor+;

wireDefinitionColor: color=wireColor_rule partNumber_rule;

wireCrossSection_rule: crossSection=(POSITIVE_FLOAT | POSITIVE_NUMBER) KEYWORD_SQMM;
wireInsulation_rule: KEYWORD_INSULATION KEYWORD_DIAMETER diameter=(POSITIVE_FLOAT | POSITIVE_NUMBER) KEYWORD_MM;
wireColor_rule: color1=COLOR_NAME (PIN_SEPARATOR color2=COLOR_NAME)? ;

currentRating_rule: KEYWORD_CURRENT KEYWORD_UP KEYWORD_TO current=(POSITIVE_FLOAT | POSITIVE_NUMBER) KEYWORD_AMPS;


// connector type description
connectorDefinition: KEYWORD_DEFINE
    connectorModel
    KEYWORD_CONNECTOR
    pinNaming_rule?
    KEYWORD_IS connectorComponent+ pinDefinition_rule+ ;

pinNaming_rule: KEYWORD_PIN KEYWORD_NAMES pinName_rule+ ;

pinName_rule: from=TEXT (SIZE_SEPARATOR to=TEXT)? ;


connectorComponent: ( KEYWORD_HOUSING | KEYWORD_ACCESSORY | (KEYWORD_CAVITY KEYWORD_PLUG ) )
                    ( count=POSITIVE_NUMBER KEYWORD_PCS )?
                    partNumber_rule
                    ;

partNumber_rule: pn=TEXT (KEYWORD_FROM vendor=TEXT)?;

connectorModel: pinCount_rule
                    (female=KEYWORD_FEMALE | male=KEYWORD_MALE | (KEYWORD_NO KEYWORD_GENDER) )
                    family=TEXT;

connectorModelRef: pinCount_rule family=TEXT (KEYWORD_VARIANT variant=TEXT)?;

pinCount_rule:  ( (single=KEYWORD_SINGLE KEYWORD_PIN) | (pinCount=POSITIVE_NUMBER KEYWORD_PINS) );

pinDefinition_rule:  section=pinSection_rule? pinComponent_rule+;

pinSection_rule: KEYWORD_SECTION TEXT ;

pinComponent_rule: (pinPartNumber_rule | sealPartNumber_rule ) ;

pinPartNumber_rule: KEYWORD_PIN partNumber_rule KEYWORD_FOR
                        pinCrossSection_rule
                        pinInsulation_rule ;

pinCrossSection_rule:   crossFrom=wireCrossSection_rule
                        SIZE_SEPARATOR
                        crossTo=wireCrossSection_rule ;

pinInsulation_rule: KEYWORD_INSULATION KEYWORD_DIAMETER
                    insulationFrom=insulationDiameter_rule
                    SIZE_SEPARATOR
                    insulationTo=insulationDiameter_rule ;

insulationDiameter_rule:  diameter=(POSITIVE_FLOAT | POSITIVE_NUMBER) KEYWORD_MM;

sealPartNumber_rule: KEYWORD_SEAL partNumber_rule KEYWORD_FOR pinInsulation_rule ;

// device description
device: KEYWORD_DEVICE name=TEXT device_connector+;
device_connector: KEYWORD_USING KEYWORD_CONNECTOR connector_type=TEXT KEYWORD_WITH device_signal+;
device_signal: KEYWORD_SIGNAL name=TEXT KEYWORD_ON KEYWORD_PIN pin=TEXT;

// device attachment
deviceAttachment: KEYWORD_DEVICE devName=TEXT KEYWORD_IS devType=TEXT
    KEYWORD_CONNECTED KEYWORD_VIA deviceAttachmentConnector+;
deviceAttachmentConnector: name=CONNECTOR_NAME;

// signal definition
signalName: SIGNAL_NAME_FORM1 | SIGNAL_NAME_FORM2 | TEXT;
signal: KEYWORD_SIGNAL signalName KEYWORD_IS description=TEXT
        signalSpecification_rule
        KEYWORD_CONNECTS signalConnection signalConnection+;

signalSpecification_rule: currentRating_rule wireColor_rule? ;

signalConnection: (
    ( conn=CONNECTOR_NAME PIN_SEPARATOR pinRef=pinRef_rule)
                    );

pinRef_rule: POSITIVE_NUMBER   #pinNumber
            | TEXT              #pinName
            ;


// physical layout definition
path:   KEYWORD_PATH KEYWORD_BETWEEN KEYWORD_JUNCTION junction=JUNCTION_NAME path_element+;

path_element: KEYWORD_AND ( path_element_connector=KEYWORD_CONNECTOR | path_element_junction=KEYWORD_JUNCTION )
                name=(CONNECTOR_NAME | JUNCTION_NAME)
                KEYWORD_WITH KEYWORD_LENGTH length=SIGNAL_NAME_FORM2;

connector: KEYWORD_CONNECTOR name=CONNECTOR_NAME description=TEXT
            KEYWORD_IS connectorModelRef;

