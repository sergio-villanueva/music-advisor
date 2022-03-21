# Music Advisor
A Client App that connects to the Spotify Web API to bring a host of services such as featured playlists, spotify categories, new albums, and playlists by category to users. Project code is found under the "music-advisor/Music Advisor/task/" path.

## Authorization
The Music Advisor follows the OAuth 2.0 Authorization Code Flow outlined in detail at the Spotify for Developers [Development Guide](https://developer.spotify.com/documentation/general/guides/authorization/code-flow/). In short, users must grant permission to the app in order to use it. Here is the flow as described by Spotify: ![Code Grant](https://developer.spotify.com/assets/AuthG_AuthoriztionCode.png)
## API Access
The Client App employs the MVC Architecture to add a layer of abstraction between the physical Spotify Data (Models) and Business Logic. ![MVC](https://developer.mozilla.org/en-US/docs/Glossary/MVC/model-view-controller-light-blue.png)
For scalability purposes, controllers are implemented using the Strategy Design Pattern which takes care of establishing the API connection.
## User Navigation
On Start Up, users must enter one of the following commands:
- "auth": starts the authorization process. Users will be redirected to Spotify for application authorization. Once completed, users are now able to access data with the following following commands.
- "new": access newly available albums on Spotify. 
- "featured": access featured playlists.
- "categories": view all the available categories on Spotify.
- "playlists": access a list of playlists by category.

Note: Due to the paginated layout, only 5 elements are shown per page. Enter "next" or "prev" to access other available pages of returned data.
