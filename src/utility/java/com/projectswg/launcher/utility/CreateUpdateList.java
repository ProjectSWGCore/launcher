/***********************************************************************************
 * Copyright (C) 2018 /// Project SWG /// www.projectswg.com                       *
 *                                                                                 *
 * This file is part of the ProjectSWG Launcher.                                   *
 *                                                                                 *
 * This program is free software: you can redistribute it and/or modify            *
 * it under the terms of the GNU Affero General Public License as published by     *
 * the Free Software Foundation, either version 3 of the License, or               *
 * (at your option) any later version.                                             *
 *                                                                                 *
 * This program is distributed in the hope that it will be useful,                 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 *                                                                                 *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.          *
 *                                                                                 *
 ***********************************************************************************/

package com.projectswg.launcher.utility;

import me.joshlarson.json.JSONArray;
import me.joshlarson.json.JSONObject;
import me.joshlarson.json.JSONOutputStream;
import net.openhft.hashing.LongHashFunction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Objects;
import java.util.zip.Adler32;

public class CreateUpdateList {
	
	public static void main(String [] args) throws IOException {
		if (args.length <= 0) {
			System.err.println("Invalid arguments. Expected: java -jar CreateUpdateList.jar <patch directory>");
			return;
		}
		File patch = new File(args[0]);
		if (!patch.isDirectory()) {
			System.err.println("Invalid patch directory - not a directory: " + patch);
			return;
		}
		patch = patch.getCanonicalFile();
		
		System.out.println("Opening " + patch + " for reading...");
		JSONArray files = new JSONArray();
		createFileList(files, patch, patch.getAbsolutePath());
		
		System.out.println("Saving to file...");
		try (JSONOutputStream out = new JSONOutputStream(new FileOutputStream(new File("files.json")))) {
			out.writeArray(files);
		}
		System.out.println("Done.");
	}
	
	private static void createFileList(JSONArray files, File directory, String filter) {
		for (File child : Objects.requireNonNull(directory.listFiles())) {
			if (child.isFile()) {
				addFile(files, child, filter);
			} else if (child.isDirectory()) {
				createFileList(files, child, filter);
			} else {
				System.err.println("Unknown file: " + child);
			}
		}
	}
	
	private static void addFile(JSONArray files, File file, String filter) {
		String path = file.getAbsolutePath().substring(filter.length());
		if (!isValidFile(file)) {
			System.out.println("    Ignoring " + path);
			return;
		}
		System.out.println("    Adding " + path);
		
		JSONObject obj = new JSONObject();
		obj.put("path", path);
		obj.put("length", file.length());
		try (FileChannel fc = FileChannel.open(file.toPath())) {
			ByteBuffer bb = fc.map(MapMode.READ_ONLY, 0, file.length());
			obj.put("adler32", getAdler32(bb));
			obj.put("xxhash", getXXHash(bb));
//			obj.put("md5", getMD5(bb));
//			obj.put("sha3", getSHA3(bb));
		} catch (IOException e) {
			e.printStackTrace();
		}
		files.add(obj);
	}
	
	private static long getAdler32(ByteBuffer bb) {
		bb.position(0);
		Adler32 adler = new Adler32();
		adler.update(bb);
		return adler.getValue();
	}
	
	private static long getXXHash(ByteBuffer bb) {
		bb.position(0);
		return LongHashFunction.xx().hashBytes(bb);
	}
	
//	private static String getMD5(ByteBuffer bb) {
//		try {
//			bb.position(0);
//			MessageDigest digest = MessageDigest.getInstance("MD5");
//			digest.update(bb);
//			return Hex.toHexString(digest.digest());
//		} catch (NoSuchAlgorithmException e) {
//			return "";
//		}
//	}
	
//	private static String getSHA3(ByteBuffer bb) {
//		bb.position(0);
//		MessageDigest digest = new SHA3.Digest512();
//		digest.update(bb);
//		return Hex.toHexString(digest.digest());
//	}
	
	private static boolean isValidFile(File file) {
		String name = file.getName();
		return !name.equals("files.json") && !name.equals("user.cfg") && !name.equals("options.cfg") && !name.endsWith(".log") && !name.endsWith(".iff");
	}
	
}
