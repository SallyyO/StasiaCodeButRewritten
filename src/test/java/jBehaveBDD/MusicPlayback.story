Meta:

Narrative:
As a music-lover
I want to play songs
So that I can enjoy music

Scenario: scenario description
Given a system state
When I do something
Then system is in a different state

Scenario: Playing just one song
Given i have a song "Test song" in my library
When I play the song
Then the playback should start
And the status should show "Playing: Test song"
