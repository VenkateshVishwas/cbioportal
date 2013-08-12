/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.cgds.scripts;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Fetches PFAM graphic data.
 *
 * @author Selcuk Onur Sumer
 */
public class FetchPfamGraphicsData
{
	public static final String URL_PREFIX = "http://pfam.sanger.ac.uk/protein/";
	public static final String URL_SUFFIX = "/graphic";

	/**
	 * Parses the given input file and creates an output with pfam graphics data
	 * for each uniprot id.
	 *
	 * @param inputFilename     name of the uniprot id mapping file
	 * @param outputFilename    name of the output pfam graphics file
	 * @param incremental       indicates incremental fetching
	 * @return  total number of errors
	 */
	public static int driver(String inputFilename,
			String outputFilename,
			boolean incremental) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(inputFilename));
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));

		String line;
		int numLines = 0;
		int numErrors = 0;

		// TODO if incremental:
		// 1. open the file in append mode, do not overwrite
		// 2. check if a certain uniprot id is already mapped in the file
		// 3. populate key set if incremental option is selected
		Set<String> keySet = initKeySet(outputFilename, incremental);

		// read all
		while ((line = in.readLine()) != null)
		{
			if (line.trim().length() == 0)
			{
				continue;
			}

			String[] parts = line.split("\t");

			if (parts.length > 1)
			{
				String uniprotId = parts[1];

				// avoid to add a duplicate entry
				if (keySet.contains(uniprotId))
				{
					continue;
				}

				String pfamJson = fetch(uniprotId);
				keySet.add(uniprotId);

				// replace all tabs and new lines with a single space
				pfamJson = pfamJson.trim().replaceAll("\t", " ").replaceAll("\n", " ");

				// verify if it is really a JSON object
				// TODO this verification may not be safe...
				if (pfamJson.startsWith("[") || pfamJson.startsWith("{"))
				{
					out.write(uniprotId);
					out.write("\t");
					out.write(pfamJson);
					out.write("\n");
				}
				else
				{
					System.out.println("Invalid data for: " + uniprotId);
					numErrors++;
				}
			}

			numLines++;
		}

		System.out.println("Total number of lines processed: " + numLines);

		out.close();
		in.close();

		return numErrors;
	}

	private static Set<String> initKeySet(String outputFilename, boolean incremental)
	{
		HashSet<String> keySet = new HashSet<String>();

		if (incremental)
		{
			// TODO populate keyset by processing output file
		}

		return keySet;
	}

	/**
	 * Fetches the JSON data from the PFAM graphics service for the
	 * specified uniprot id.
	 *
	 * @param uniprotId a uniprot id
	 * @return  pfam graphic data as a JSON string
	 * @throws  IOException
	 */
	private static String fetch(String uniprotId) throws IOException
	{
		URL url = new URL(URL_PREFIX + uniprotId + URL_SUFFIX);

		URLConnection pfamConn = url.openConnection();

		BufferedReader in = new BufferedReader(
				new InputStreamReader(pfamConn.getInputStream()));

		String line;
		StringBuilder sb = new StringBuilder();

		// read all
		while((line = in.readLine()) != null)
		{
			sb.append(line);
		}

		in.close();

		return sb.toString();
	}

	public static void main(String[] args)
	{
		// default config params
		boolean noFetch = false;     // skip fetching
		boolean incremental = false; // overwrite or append data

		// process program arguments

		int i;

		// this is for program arguments starting with a dash
		// these arguments must come before IO file names
		for (i = 0; i < args.length; i++)
		{
			if (args[i].startsWith("-"))
			{
				if (args[i].equalsIgnoreCase("-nofetch"))
				{
					noFetch = true;
				}
				else if (args[i].equalsIgnoreCase("-append"))
				{
					incremental = true;
				}
			}
			else
			{
				break;
			}
		}

		// check IO file name args
		if (args.length - i < 2)
		{
			System.out.println("command line usage:  fetchPfamGraphicsData.sh " +
			                   "<uniprot_id_mapping_file> <output_pfam_mapping_file>");
			System.exit(1);
		}

		String input = args[i];
		String output = args[i+1];

		if (noFetch)
		{
			// do nothing, just terminate
			System.out.println("-nofetch argument provided, terminating...");
			return;
		}

		try
		{
			System.out.println("Fetching started...");
			Date start = new Date();
			int numErrors = driver(input, output, incremental);
			Date end = new Date();
			System.out.println("Fetching finished.");

			double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

			System.out.println("\nTotal time elapsed: " + timeElapsed + " seconds");

			if (numErrors > 0)
			{
				System.out.println("Total number of errors: " + numErrors);
			}
		}
		catch (IOException e)
		{
			System.out.println("error processing IO files.");
			System.exit(1);
		}
	}
}
