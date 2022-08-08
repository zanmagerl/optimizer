! mmixal:= 8H LOC Data_Section
	.text ! mmixal:= 9H LOC 8B
	.p2align 2
	LOC @+(4-@)&3
	.global abs
abs	IS @
	SUBU $254,$254,16
	STOU $253,$254,8
	ADDU $253,$254,16
	SET $1,$0
	SUBU $0,$253,12
	STTU $1,$0,0
	SUBU $0,$253,12
	LDT $0,$0,0
	BNN $0,L:2
	SUBU $0,$253,12
	LDT $0,$0,0
	NEGU $0,0,$0
	JMP L:3
L:2	IS @
	SUBU $0,$253,12
	LDT $0,$0,0
L:3	IS @
	LDO $253,$254,8
	ADDU $254,$254,16
	POP 1,0
	.p2align 2
	LOC @+(4-@)&3
	.global print_char
Buffer	BYTE "1",0
	    .p2align 2
	LOC @+(4-@)&3
	    .global print_char
print_char	IS @
	SUBU $254,$254,16
	STOU $253,$254,8
	ADDU $253,$254,16
	GETA $1,Buffer
	STB $0,$1,0
	SET $255,$1
	TRAP 0,Fputs,StdOut
	LDO $253,$254,8
	ADDU $254,$254,16
	POP 0,0
	.p2align 2
	LOC @+(4-@)&3
	.global print_string
print_string	IS @
	SUBU $254,$254,24
	STOU $253,$254,16
	ADDU $253,$254,24
	GET $2,rJ
	SUBU $1,$253,24
	STOU $0,$1,0
	SUBU $0,$253,12
	SETL $1,0
	STTU $1,$0,0
	JMP L:6
L:7	IS @
	SUBU $0,$253,12
	LDT $0,$0,0
	SUBU $1,$253,24
	LDO $1,$1,0
	ADDU $0,$1,$0
	LDB $0,$0,0
	SET $4,$0
	PUSHJ $3,print_char
	PUT rJ,$2
	SUBU $0,$253,12
	SUBU $1,$253,12
	LDT $1,$1,0
	ADDU $1,$1,1
	STTU $1,$0,0
L:6	IS @
	SUBU $0,$253,12
	LDT $0,$0,0
	SUBU $1,$253,24
	LDO $1,$1,0
	ADDU $0,$1,$0
	LDB $0,$0,0
	BNZ $0,L:7
	LDO $253,$254,16
	ADDU $254,$254,24
	POP 0,0
	.p2align 2
	LOC @+(4-@)&3
	.global print_integer
print_integer	IS @
	SUBU $254,$254,24
	STOU $253,$254,16
	ADDU $253,$254,24
	GET $1,rJ
	SET $2,$0
	SUBU $0,$253,20
	STTU $2,$0,0
	SUBU $0,$253,20
	LDT $0,$0,0
	BNP $0,L:10
	SUBU $0,$253,20
	LDT $0,$0,0
	SETL $3,#a
	SET $4,$0
	SET $5,$3
	XOR $255,$4,$5
	NEGU $2,0,$5
	CSN $5,$5,$2
	NEGU $2,0,$4
	CSN $4,$4,$2
	DIVU $2,$4,$5
	NEGU $4,0,$2
	CSN $2,$255,$4
	SET $0,$2
	SLU $0,$0,32
	SR $0,$0,32
	SET $7,$0
	PUSHJ $6,print_integer
	PUT rJ,$1
	SUBU $0,$253,20
	LDT $0,$0,0
	SETL $3,#a
	SET $4,$0
	SET $5,$3
	NEGU $2,0,$5
	CSN $5,$5,$2
	NEGU $255,0,$4
	CSN $4,$4,$255
	DIVU $4,$4,$5
	GET $2,:rR
	NEGU $5,0,$2
	CSNN $2,$255,$5
	SUBU $0,$253,9
	STBU $2,$0,0
	SUBU $0,$253,9
	LDB $0,$0,0
	ADDU $0,$0,48
	SLU $0,$0,56
	SR $0,$0,56
	SET $7,$0
	PUSHJ $6,print_char
	PUT rJ,$1
L:10	IS @
	LDO $253,$254,16
	ADDU $254,$254,24
	POP 0,0
	.section	.rodata
	.p2align 2
	LOC @+(4-@)&3
LC:0	IS @
	BYTE "This is a solution number ",#0
	.p2align 2
	LOC @+(4-@)&3
LC:1	IS @
	BYTE #a,#a,#0
	.p2align 2
	LOC @+(4-@)&3
LC:2	IS @
	BYTE #9,"Q",#0
	.p2align 2
	LOC @+(4-@)&3
LC:3	IS @
	BYTE #9,"-",#0
	.text ! mmixal:= 9H LOC 8B
	.p2align 2
	LOC @+(4-@)&3
	.global print
print	IS @
	SUBU $254,$254,32
	STOU $253,$254,24
	ADDU $253,$254,32
	GET $3,rJ
	SET $5,$0
	SUBU $0,$253,32
	STOU $2,$0,0
	SUBU $0,$253,20
	SET $7,$5
	STTU $7,$0,0
	SUBU $0,$253,24
	STTU $1,$0,0
	GETA $7,LC:0
	PUSHJ $6,print_string
	PUT rJ,$3
	SUBU $0,$253,24
	SUBU $1,$253,24
	LDT $1,$1,0
	ADDU $1,$1,1
	STTU $1,$0,0
	SUBU $0,$253,24
	LDT $0,$0,0
	SET $7,$0
	PUSHJ $6,print_integer
	PUT rJ,$3
	SETL $7,#a
	PUSHJ $6,print_char
	PUT rJ,$3
	SUBU $0,$253,12
	SETL $1,#1
	STTU $1,$0,0
	JMP L:12
L:13	IS @
	SETL $7,#9
	PUSHJ $6,print_char
	PUT rJ,$3
	SUBU $0,$253,12
	LDT $0,$0,0
	SET $7,$0
	PUSHJ $6,print_integer
	PUT rJ,$3
	SUBU $0,$253,12
	SUBU $1,$253,12
	LDT $1,$1,0
	ADDU $1,$1,1
	STTU $1,$0,0
L:12	IS @
	SUBU $1,$253,12
	SUBU $0,$253,20
	LDT $1,$1,0
	LDT $0,$0,0
	CMP $0,$1,$0
	BNP $0,L:13
	SUBU $0,$253,12
	SETL $1,#1
	STTU $1,$0,0
	JMP L:14
L:19	IS @
	GETA $7,LC:1
	PUSHJ $6,print_string
	PUT rJ,$3
	SUBU $0,$253,12
	LDT $0,$0,0
	SET $7,$0
	PUSHJ $6,print_integer
	PUT rJ,$3
	SUBU $0,$253,16
	SETL $1,#1
	STTU $1,$0,0
	JMP L:15
L:18	IS @
	SUBU $0,$253,12
	LDT $0,$0,0
	SLU $0,$0,2
	SUBU $1,$253,32
	LDO $1,$1,0
	ADDU $0,$1,$0
	LDT $0,$0,0
	SUBU $1,$253,16
	LDT $1,$1,0
	SLU $0,$0,32
	SR $0,$0,32
	CMP $0,$1,$0
	BNZ $0,L:16
	GETA $7,LC:2
	PUSHJ $6,print_string
	PUT rJ,$3
	JMP L:17
L:16	IS @
	GETA $7,LC:3
	PUSHJ $6,print_string
	PUT rJ,$3
L:17	IS @
	SUBU $0,$253,16
	SUBU $1,$253,16
	LDT $1,$1,0
	ADDU $1,$1,1
	STTU $1,$0,0
L:15	IS @
	SUBU $1,$253,16
	SUBU $0,$253,20
	LDT $1,$1,0
	LDT $0,$0,0
	CMP $0,$1,$0
	BNP $0,L:18
	SUBU $0,$253,12
	SUBU $1,$253,12
	LDT $1,$1,0
	ADDU $1,$1,1
	STTU $1,$0,0
L:14	IS @
	SUBU $1,$253,12
	SUBU $0,$253,20
	LDT $1,$1,0
	LDT $0,$0,0
	CMP $0,$1,$0
	BNP $0,L:19
	SETL $7,#a
	PUSHJ $6,print_char
	PUT rJ,$3
	SUBU $0,$253,24
	LDT $0,$0,0
	LDO $253,$254,24
	ADDU $254,$254,32
	POP 1,0
	.p2align 2
	LOC @+(4-@)&3
	.global place
place	IS @
	SUBU $254,$254,32
	STOU $253,$254,24
	ADDU $253,$254,32
	SET $4,$0
	SUBU $0,$253,32
	STOU $2,$0,0
	SUBU $0,$253,20
	SET $2,$4
	STTU $2,$0,0
	SUBU $0,$253,24
	STTU $1,$0,0
	SUBU $0,$253,12
	SETL $1,#1
	STTU $1,$0,0
	JMP L:22
L:28	IS @
	SUBU $0,$253,12
	LDT $0,$0,0
	SLU $0,$0,2
	SUBU $1,$253,32
	LDO $1,$1,0
	ADDU $0,$1,$0
	LDT $0,$0,0
	SUBU $1,$253,24
	LDT $1,$1,0
	SLU $0,$0,32
	SR $0,$0,32
	CMP $0,$1,$0
	BNZ $0,L:23
	SETL $0,0
	JMP L:24
L:23	IS @
	SUBU $0,$253,12
	LDT $0,$0,0
	SLU $0,$0,2
	SUBU $1,$253,32
	LDO $1,$1,0
	ADDU $0,$1,$0
	LDT $1,$0,0
	SUBU $0,$253,24
	LDT $0,$0,0
	SUBU $0,$1,$0
	SET $1,$0
	SLU $0,$1,32
	SR $0,$0,32
	BNN $0,L:25
	NEGU $0,0,$1
	SET $1,$0
L:25	IS @
	SET $2,$1
	SUBU $1,$253,12
	SUBU $0,$253,20
	LDT $1,$1,0
	LDT $0,$0,0
	SUBU $0,$1,$0
	SET $1,$0
	SLU $0,$1,32
	SR $0,$0,32
	BNN $0,L:26
	NEGU $0,0,$1
L:26	IS @
	SLU $1,$2,32
	SR $1,$1,32
	SLU $0,$0,32
	SR $0,$0,32
	CMP $0,$1,$0
	BNZ $0,L:27
	SETL $0,0
	JMP L:24
L:27	IS @
	SUBU $0,$253,12
	SUBU $1,$253,12
	LDT $1,$1,0
	ADDU $1,$1,1
	STTU $1,$0,0
L:22	IS @
	SUBU $0,$253,20
	LDT $0,$0,0
	SUBU $0,$0,1
	SET $2,$0
	SUBU $0,$253,12
	LDT $0,$0,0
	SLU $1,$0,32
	SR $1,$1,32
	SLU $0,$2,32
	SR $0,$0,32
	CMP $0,$1,$0
	BNP $0,L:28
	SETL $0,#1
L:24	IS @
	LDO $253,$254,24
	ADDU $254,$254,32
	POP 1,0
	.p2align 2
	LOC @+(4-@)&3
	.global queen
queen	IS @
	SUBU $254,$254,40
	STOU $253,$254,32
	ADDU $253,$254,40
	GET $4,rJ
	SET $6,$0
	SUBU $0,$253,40
	STOU $3,$0,0
	SUBU $0,$253,20
	SET $9,$6
	STTU $9,$0,0
	SUBU $0,$253,24
	STTU $1,$0,0
	SUBU $0,$253,28
	SET $1,$2
	STTU $1,$0,0
	SUBU $0,$253,12
	SETL $1,#1
	STTU $1,$0,0
	JMP L:30
L:33	IS @
	SUBU $2,$253,40
	SUBU $0,$253,12
	LDT $0,$0,0
	SLU $1,$0,32
	SR $1,$1,32
	SUBU $0,$253,20
	LDT $0,$0,0
	LDO $10,$2,0
	SET $9,$1
	SET $8,$0
	PUSHJ $7,place
	PUT rJ,$4
	SET $0,$7
	SLU $0,$0,32
	SR $0,$0,32
	BZ $0,L:31
	SUBU $0,$253,20
	LDT $0,$0,0
	SLU $0,$0,2
	SUBU $1,$253,40
	LDO $1,$1,0
	ADDU $0,$1,$0
	SUBU $1,$253,12
	LDT $2,$1,0
	STTU $2,$0,0
	SUBU $1,$253,20
	SUBU $0,$253,24
	LDT $1,$1,0
	LDT $0,$0,0
	CMP $0,$1,$0
	BNZ $0,L:32
	SUBU $2,$253,28
	SUBU $3,$253,40
	SUBU $0,$253,28
	LDT $0,$0,0
	SLU $1,$0,32
	SR $1,$1,32
	SUBU $0,$253,24
	LDT $0,$0,0
	LDO $10,$3,0
	SET $9,$1
	SET $8,$0
	PUSHJ $7,print
	PUT rJ,$4
	STTU $7,$2,0
	JMP L:31
L:32	IS @
	SUBU $0,$253,20
	LDT $0,$0,0
	ADDU $0,$0,1
	SET $6,$0
	SUBU $3,$253,28
	SUBU $5,$253,40
	SUBU $0,$253,28
	LDT $0,$0,0
	SLU $2,$0,32
	SR $2,$2,32
	SUBU $0,$253,24
	LDT $0,$0,0
	SLU $1,$0,32
	SR $1,$1,32
	SLU $0,$6,32
	SR $0,$0,32
	LDO $11,$5,0
	SET $10,$2
	SET $9,$1
	SET $8,$0
	PUSHJ $7,queen
	PUT rJ,$4
	STTU $7,$3,0
L:31	IS @
	SUBU $0,$253,12
	SUBU $1,$253,12
	LDT $1,$1,0
	ADDU $1,$1,1
	STTU $1,$0,0
L:30	IS @
	SUBU $1,$253,12
	SUBU $0,$253,24
	LDT $1,$1,0
	LDT $0,$0,0
	CMP $0,$1,$0
	BNP $0,L:33
	SUBU $0,$253,28
	LDT $0,$0,0
	LDO $253,$254,32
	ADDU $254,$254,40
	POP 1,0
	.section	.rodata
	.p2align 2
	LOC @+(4-@)&3
LC:4	IS @
	BYTE "There are total ",#0
	.p2align 2
	LOC @+(4-@)&3
LC:5	IS @
	BYTE " solutions for 8-queens problem.",#a,#0
	.text ! mmixal:= 9H LOC 8B
	.p2align 2
	LOC @+(4-@)&3
	.global main
main	IS @
	SUBU $254,$254,48
	STOU $253,$254,40
	ADDU $253,$254,48
	GET $0,rJ
	SUBU $1,$253,12
	SETL $2,#8
	STTU $2,$1,0
	SUBU $1,$253,16
	SETL $2,0
	STTU $2,$1,0
	SUBU $3,$253,16
	SUBU $4,$253,48
	SUBU $1,$253,16
	LDT $1,$1,0
	SLU $2,$1,32
	SR $2,$2,32
	SUBU $1,$253,12
	LDT $1,$1,0
	SET $9,$4
	SET $8,$2
	SET $7,$1
	SETL $6,#1
	PUSHJ $5,queen
	PUT rJ,$0
	STTU $5,$3,0
	GETA $6,LC:4
	PUSHJ $5,print_string
	PUT rJ,$0
	SUBU $1,$253,16
	LDT $1,$1,0
	SET $6,$1
	PUSHJ $5,print_integer
	PUT rJ,$0
	GETA $6,LC:5
	PUSHJ $5,print_string
	PUT rJ,$0
	SETL $0,0
	LDO $253,$254,40
	ADDU $254,$254,48
	POP 1,0
	.data ! mmixal:= 8H LOC 9B
Main	IS @
	SETL $0,64
	PUT 20,$0
	SETL $0,64
	PUT 19,$0
	SETH $254,#4000
	SETH $253,#4000
	SETH $252,#3000
	PUSHJ 2,main
