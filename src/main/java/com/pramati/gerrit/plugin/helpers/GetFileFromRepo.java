package com.pramati.gerrit.plugin.helpers;

//Copyright (C) 2009 The Android Open Source Project
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.Patch;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.ChangeControl;
import com.google.gerrit.server.project.NoSuchChangeException;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Helper class to get file from the gerrit repository
 * 
 * @author dilip
 * 
 */
public class GetFileFromRepo {
	private static Provider<ReviewDb> requestDb;
	private static GitRepositoryManager repoManager;
	private static ChangeControl.Factory changeControl;

	@Inject
	public GetFileFromRepo(final GitRepositoryManager grm,
			final Provider<ReviewDb> sf, final ChangeControl.Factory ccf) {
		requestDb = sf;
		repoManager = grm;
		changeControl = ccf;
	}

	/**
	 * returns the File Stream from the gerrit repository. returns "null" if the
	 * given file not found in the repository.<br>
	 * patchStr should like in given format::"changeid/patchsetID/filename" <br>
	 * eg: 1/2/readme.md
	 * 
	 * @param patchStr
	 * @return
	 * @throws IOException
	 */
	public static BufferedInputStream doGetFile(String patchStr)
			throws IOException {
		final Patch.Key patchKey;
		final Change.Id changeId;
		final Project project;
		final PatchSet patchSet;
		final Repository repo;
		final ReviewDb db;
		final ChangeControl control;
		try {
			patchKey = Patch.Key.parse(patchStr);
		} catch (NumberFormatException e) {
			return null;
		}
		changeId = patchKey.getParentKey().getParentKey();

		try {
			db = requestDb.get();
			control = changeControl.validateFor(changeId);

			project = control.getProject();
			patchSet = db.patchSets().get(patchKey.getParentKey());
			if (patchSet == null) {
				return null;
				// rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (NoSuchChangeException e) {
			// rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		} catch (OrmException e) {
			// getServletContext().log("Cannot query database", e);
			// rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

		try {
			repo = repoManager.openRepository(project.getNameKey());
		} catch (RepositoryNotFoundException e) {
			// getServletContext().log("Cannot open repository", e);
			// rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

		final ObjectLoader blobLoader;
		final RevCommit fromCommit;
		final String path = patchKey.getFileName();
		try {
			final ObjectReader reader = repo.newObjectReader();
			try {
				final RevWalk rw = new RevWalk(reader);
				final RevCommit c;
				final TreeWalk tw;

				c = rw.parseCommit(ObjectId.fromString(patchSet.getRevision()
						.get()));
				fromCommit = c;

				tw = TreeWalk.forPath(reader, path, fromCommit.getTree());
				if (tw == null) {
					// rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
					return null;
				}

				if (tw.getFileMode(0).getObjectType() == Constants.OBJ_BLOB) {
					blobLoader = reader.open(tw.getObjectId(0),
							Constants.OBJ_BLOB);

				} else {
					// rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return null;
				}
			} finally {
				reader.release();
			}
		} catch (IOException e) {
			// getServletContext().log("Cannot read repository", e);
			// rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		} catch (RuntimeException e) {
			// getServletContext().log("Cannot read repository", e);
			// rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		} finally {
			repo.close();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		blobLoader.copyTo(out);
		byte[] b = out.toByteArray();
		BufferedInputStream br = new BufferedInputStream(
				new ByteArrayInputStream(b));
		return br;
	}
}
