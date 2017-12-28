name := "proj1"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.8"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.5.8"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.5.8"

libraryDependencies += "org.iq80.leveldb"            % "leveldb"          % "0.7"
libraryDependencies += "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"

