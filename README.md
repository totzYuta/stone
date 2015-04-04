# stone

Minimum Script Programming Language in Java

It is a programming language created in the book, "スクリプト言語の作り方(技術評論社)".


## Rules

- Needs no variable declarations.
  - Have no variable type, but can generate an error when incorrect programming is attenpted.
- Needs no semicolon 
- A variable in the last line is a result of program


## Example

```
even = 10
odd = 0
i = 1
while i < 10 {
    if i % 2 == 0 { // even number?
          even = even + i
            } else {
                  odd = odd + i
                    }
                      i = i + 1
}
even + odd
```
