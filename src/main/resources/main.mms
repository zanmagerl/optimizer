Main    IS	@
        SETL $0,64
        PUT 20,$0
        SETL $0,64
        PUT 19,$0
        SETH $254,#4000
        SETH $253,#4000
        PUSHJ 2,main
        TRAP  0,Halt,0