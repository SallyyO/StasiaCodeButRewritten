Meta:

Narrative:
As a music-lover
I want to be able to manage my collection of songs
So that I can organise my music

Scenario: Adding a new song
Given I have a valid audio file at "path/to/song.mp3"
When I create a song with the title "Very good song" and artist "Fav Artist"
Then the song should be saved with an Id
And the song should show up in my list of songs

Scenario: Searching for songs
Given I have songs in my library:
|   title   |    artist   |
| Rock song | Rock artist |
| Jazz song | Jazz artist |
| Pop song  | Pop artist  |
When I search for "Rock"
Then I should only see 1 song in the search results
And the result should be "Rock Song"

