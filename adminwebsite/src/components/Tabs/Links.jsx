import React, { useEffect, useState } from 'react';
import { supabase } from '../../supabaseClient'; // ✅ Import Supabase Client
import TopNav from "../NavBars/TopNav";

const Links = () => {
    const [links, setLinks] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let ignore = false;

        const fetchLinks = async () => {
            try {
                // Fetch links from Supabase
                const { data, error } = await supabase
                    .from('links')
                    .select(`
                        *,
                        users (userusername)  // Fetch userusername from the users table
                    `);
                
                if (error) {
                    console.error('Error fetching links:', error.message);
                } else {
                    // Format date_verified
                    const updatedLinks = data.map(link => ({
                        ...link,
                        date_verified: new Date(link.date_verified).toLocaleString(),
                        userusername: link.users ? link.users.userusername : null // Get userusername
                    }));

                    // Remove duplicates based on link_id
                    const uniqueLinks = Array.from(
                        new Map(
                            updatedLinks
                                .filter(link => link.link_id !== undefined)
                                .map(link => [link.link_id, link])
                        ).values()
                    );

                    if (!ignore) {
                        setLinks(uniqueLinks);
                    }
                    console.log('Fetched links data:', uniqueLinks);
                }
            } catch (error) {
                console.error('Error fetching links:', error.message);
            } finally {
                if (!ignore) {
                    setLoading(false);
                }
            }
        };

        fetchLinks();

        return () => {
            ignore = true;
        };
    }, []);

    if (loading) {
        return <h1>Loading...</h1>;
    }

    return (
        <>
            <TopNav />
            <h1 className='links-header'>Malicious Links</h1>
            <table>
                <thead>
                    <tr>
                        <th>URL</th>
                        <th>Top-Level Domain</th>
                        <th>Reported By</th>
                        <th>Date and Time Verified</th>
                    </tr>
                </thead>
                <tbody>
                    {links.map((link) => (
                        <tr key={link.link_id}>
                            <td>{link.url_link}</td>
                            <td>{link.tld}</td>
                            <td>{link.userusername}</td> {/* Display userusername */}
                            <td>{link.date_verified}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
};

export default Links;
