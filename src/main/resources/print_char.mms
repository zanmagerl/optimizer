Buffer	BYTE	"1",0
	    .p2align 2
	    LOC @+(4-@)&3
	    .global print_char
print_char	IS @
        SUBU $254,$254,16
        STOU $253,$254,8
        ADDU $253,$254,16
        GETA	$1,Buffer
        STB		$0,$1,0
        SET		$255,$1
        TRAP	0,Fputs,StdOut
        LDO $253,$254,8
        ADDU $254,$254,16
        POP 0,0