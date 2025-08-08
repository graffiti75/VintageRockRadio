import Foundation

class SongParser {
    func parseSongs(decade: String) -> [Song] {
        var songs = [Song]()
        if let path = Bundle.main.path(forResource: "ids", ofType: "txt") {
            do {
                let data = try String(contentsOfFile: path, encoding: .utf8)
                let lines = data.components(separatedBy: .newlines)
                for line in lines {
                    let parts = line.split(separator: ";")
                    if parts.count == 5 {
                        let songDecade = String(parts[0]).trimmingCharacters(in: .whitespaces)
                        if songDecade == decade {
                            let song = Song(
                                decade: songDecade,
                                year: String(parts[1]).trimmingCharacters(in: .whitespaces),
                                band: String(parts[2]).trimmingCharacters(in: .whitespaces),
                                song: String(parts[3]).trimmingCharacters(in: .whitespaces),
                                youtubeID: String(parts[4]).trimmingCharacters(in: .whitespaces)
                            )
                            songs.append(song)
                        }
                    }
                }
            } catch {
                print("Error reading file: \(error)")
            }
        }
        return songs
    }
}
