lexer grammar CableDesignerLexer;

COMMENT : '//' .*? '\n'  -> skip;

KEYWORD_PATH: 'path';
KEYWORD_BETWEEN: 'between';
KEYWORD_JUNCTION: 'junction';
KEYWORD_AND: 'and';
KEYWORD_CONNECTOR: 'connector';
KEYWORD_WITH: 'with';
KEYWORD_LENGTH: 'length';
KEYWORD_MM: 'mm';
KEYWORD_CM: 'cm';
KEYWORD_INCLUDE: 'include';
KEYWORD_USING: 'using';
KEYWORD_TYPE: 'type';
KEYWORD_PIN: 'pin';
KEYWORD_SIGNAL: 'signal';
KEYWORD_ON: 'on';
KEYWORD_DEVICE: 'device';
KEYWORD_IS: 'is';
KEYWORD_CONNECTED: 'connected';
KEYWORD_VIA: 'via';
KEYWORD_CONNECTS: 'connects';
KEYWORD_PINS: 'pins';
KEYWORD_SINGLE: 'single';
KEYWORD_MALE: 'male';
KEYWORD_FEMALE: 'female';
KEYWORD_NO: 'no';
KEYWORD_GENDER: 'gender';
KEYWORD_DEFINE: 'define';
KEYWORD_VARIANT: 'variant';
KEYWORD_HOUSING: 'housing';
KEYWORD_COVER: 'cover';
KEYWORD_SEAL: 'seal';
KEYWORD_PCS: 'pcs';
KEYWORD_FROM: 'from';
KEYWORD_WIRE: 'wire';
KEYWORD_SQMM: 'sqmm';
KEYWORD_INSULATION: 'insulation';
KEYWORD_DIAMETER: 'diameter';
KEYWORD_CURRENT: 'current';
KEYWORD_UP: 'up';
KEYWORD_TO: 'to';
KEYWORD_AMPS: 'amps';
KEYWORD_CAVITY: 'cavity';
KEYWORD_PLUG: 'plug';
KEYWORD_SECTION: 'section';
KEYWORD_FOR: 'for';
KEYWORD_ACCESSORY: 'accessory';
KEYWORD_NAMES: 'names';
KEYWORD_DOCUMENT: 'document';
KEYWORD_ORDERED: 'ordered';


POSITIVE_FLOAT: [0-9]+ '.' [0-9]+;

POSITIVE_NUMBER: [1-9][0-9]*;

CONNECTOR_NAME: 'C' [0-9]+ [A-Z]? [F|M]?;
JUNCTION_NAME: 'J' [0-9]+;



SIGNAL_NAME_FORM1: [A-Z]+ '-' [0-9]+;
SIGNAL_NAME_FORM2: [0-9]+ [a-zA-Z0-9]*;

PIN_SEPARATOR: '/';

SIZE_SEPARATOR: '...';

COLOR_NAME:  'white'
            |'black'
            |'blue'
            |'green'
            |'brown'
            |'red'
            |'yellow'
            |'gray'
            |'orange'
            |'violet'
            |'pink';



TEXT : '"' ~["]* '"' ;

WS : [\r\t\n ] -> skip ;
ANY : .;