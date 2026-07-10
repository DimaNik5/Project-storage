
; Функция выводящая символ, сохраненый в BX
; Аргументы: BX - символ
print_c:
    push ax

    mov al, bl
    mov ah, 0x0E        ; Функция BIOS для вывода символа
    mov bx, 0x000F      ; Атрибуты вывода
    int 0x10

    pop ax
    ret

; Функция выводящая число в битовом формате
; Аргументы: BX - число
print_bites:
    push cx
    push bx

    ; Выводим префикс "0b"
    mov bx, '0'
    call print_c
    mov bx, 'b'
    call print_c

    pop bx              ; восстанавливаем исходное число
    mov cx, 16          ; 16 бит для вывода

@@bit_loop:
    ; Сдвигаем старший бит в CF
    shl bx, 1
    
    ; Выводим бит в зависимости от CF
    jc @@output_one
    mov ax, '0'
    jmp @@output
@@output_one:
    mov ax, '1'
@@output:
    push bx
    mov bx, ax
    call print_c
    pop bx
    
    loop @@bit_loop     ; cx-- и переход если cx != 0

    pop cx
    ret

; Функция выводящее число в восмеричном формате
; Аргументы: BX - число
print_oct:
    push ax
    push cx

    mov cx, 15
    push bx
    mov bx, '0'
    call print_c
    mov bx, 'o'
    call print_c
    pop bx

@@oct_loop:

    mov ax, bx           ; копируем значение
    shr ax, cl           ; сдвигаем так, чтобы 3 бита стали младшими
    and ax, 7            ; изолируем 3 младших бита

    push bx
    add ax, '0'
    mov bx, ax
    call print_c
    pop bx

    cmp cx, 0
    je @@oct_end
    sub cx, 3
    jmp @@oct_loop

@@oct_end:
    pop cx
    pop ax
    ret


; Функция выводящее число в шестнадцатиричном формате
; Аргументы: BX - число
print_hex:
    push ax
    push cx

    mov cx, 12
    push bx
    mov bx, '0'
    call print_c
    mov bx, 'x'
    call print_c
    pop bx

@@hex_loop:

    mov ax, bx           ; копируем значение
    shr ax, cl           ; сдвигаем так, чтобы 4 бита стали младшими
    and ax, 15           ; изолируем 4 младших бита

    push bx
    cmp ax, 10
    jae @@greater_or_equal_10
@@hex_ret:
    add ax, '0'
    mov bx, ax
    call print_c
    pop bx

    cmp cx, 0
    je @@hex_end
    sub cx, 4
    jmp @@hex_loop

@@greater_or_equal_10:
    add ax, 7
    jmp @@hex_ret

@@hex_end:
    pop cx
    pop ax
    ret


