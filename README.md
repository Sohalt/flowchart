# flowchart

A Clojure library designed to ... well, that part is up to you.

## Usage

FIXME

Stylus:
(left)click -> pie menu w/ selection: start,end, stmt, branch
(left)click on existing bubble -> focus text label
(left)click on existing bubble & drag to other bubble -> create arrow (from 1st to 2nd)
(right)click&drag on bubble -> move bubble
(right)click&drag on canvas -> move canvas
(middle)click&drag -> zoom
eraser -> erase

touch:
1 finger -> pie menu
1 finger on existing bubble -> focus text label
1 finger on existing bubble & drag to other bubble -> create arrow (from 1st to 2nd)
2 finger&drag -> move
2 finger&pinch -> zoom

mouse:
leftclick -> pie menu
leftclick on existing bubble -> focus text label
leftclick on existing bubble & drag to other bubble -> create arrow (from 1st to 2nd)
rightclick -> erase
middleclick&drag -> move
scroll -> zoom


improvement:
avy like jumping to text labels

alternate modes:
determine type by number and direction of arrows:
incoming outgoing type
0 0 note
0 1 start
n 0 end
n 1 stmt
n n branch

determine type by keypress
q note
w start
e end
r stmt
t branch

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
