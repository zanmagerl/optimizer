Buffer	BYTE	"1",0
	    .p2align 2
	    LOC @+(4-@)&3
	    .global print_char
print_char	IS @
        GETA	$1,Buffer
        STB		$0,$1,0
        SET		$255,$1
        TRAP	0,Fputs,StdOut
        POP 0,0