https://gitlab.com/g4660/compilatori_2023_24/toy2-semancgen-es5/iemmino_es5.git




## ScopingVisitor
Si occupa di popolare il **type environment** dell'intero programma (salvandolo nella proprietà "environment" dei `BodyNode`) e di effettuare l'**inferenza di tipo**. Si occupa inoltre di rinominare tutti gli identifiers, definiti nel programma Toy2 di input, che vanno in conflitto con keywords del C (ad esempio "continue", "break", etc) e/o in confilitto con identifiers definiti personalmente per utilità (ad esempio è stata definita come funzione di utilità "freeAll" per liberare la memoria in C: se il programma Toy2 scritto ha una funzione con questo nome, esso sarà cambiato). Gestisce anche gli errori di **dichiarazione multipla**.

## SemanticAnalysisVisitor 
Effettua l'analisi semantica di Toy2 e ferma la compilazione in linguaggio C se vengono individuati errori. L'individuazione di errori **non si ferma al primo errore incontrato**, ma continua l'analisi per individuarne altri (nel caso ce ne siano più di uno): ad esempio verrà generato un errore per ogni utilizzo di una variabile non dichiarata, così come anche altri errori di tipo diverso.

## CodeGeneratorVisitor 
Si occupa di generare il codice C, correttamente indentato (utile per eventuale debugging) e con alcune funzioni di utility: conversione di altri tipi in stringa per la concatenazione, deallocazione di memoria delle stringhe. Le funzioni con un solo valore di ritorno diventano funzioni normali in C, le funzioni con più di un valore di ritorno vengono dichiarate come void e tutti i valori di ritorno vengono effettuati tramite passaggio per riferimento.

## Test files

Nella directory `test_files`, è presente l'esercizio sulla calcolatrice e 4 nuovi test invalid.

