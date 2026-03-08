# TranslatorInator
Przedstawiony projekt to kod źródłowy wtyczki do środowiska IntelliJ IDEA o nazwie TranslatorInator. Wtyczka ta ma na celu pomaganie programistom w automatycznym tłumaczeniu nazw zmiennych i innych elementów kodu na dowolny wybrany język przy użyciu <kbd>alt + enter</kbd> (quick fixes).

[bez tytułu.webm](https://github.com/user-attachments/assets/bb883274-f11d-4a61-8722-8243b1cd7aca)

## Cechy
- Wybór języka docelowego (domyślnie English)
- Wybór modelu językowego w ramach API groq (domyślnie openai/gpt-oss-120b))
- Klucz API przechowywany w bezpiecznej formie (jak hasła)
- Tłumacznie na popularne skróty np. <kbd>maksymalnyRozmiar</kbd> => <kbd>maxSize</kbd>
- Tłumaczenie przy użyciu refactoringu, pozwala na zmianę nazwy we wszystkich miejscach użycia
- Zachowanie konwencji nazewniczej przy tłumaczeniu, dzięku odpowiednio stworzonemu *sytem promptowi* 


## Autorzy
- matio004 - 251565
- gabsonsz - 251645
