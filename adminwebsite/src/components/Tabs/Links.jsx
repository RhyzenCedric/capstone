import React, { useEffect, useState } from 'react';
import axios from 'axios';
import TopNav from "../NavBars/TopNav";

const Links = () => {
    const [links, setLinks] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let ignore = false;

        const fetchLinks = async () => {
            try {
                const response = await axios.get('http://localhost:5000/links');
                console.log('Fetched links:', response.data);

                const updatedLinks = response.data.map(link => ({
                    ...link,
                    date_verified: new Date(link.date_verified).toLocaleString()
                }));

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
            } catch (error) {
                console.error('Error fetching links:', error);
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
                    {links.map((link) => {
                        console.log("Rendering row for:", link.link_id);
                        return (
                            <tr key={link.link_id}>
                                <td>{link.url_link}</td>
                                <td>{link.tld}</td>
                                <td>{link.reported_by}</td>
                                <td>{link.date_verified}</td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </>
    );
};

export default Links;
