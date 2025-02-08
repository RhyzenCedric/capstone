import React, { useEffect, useState } from 'react';
import axios from 'axios';
import TopNav from "../NavBars/TopNav";

const Links = () => {
    const [links, setLinks] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchLinks();
    }, []);

    const fetchLinks = async () => {
        try {
            const response = await axios.get('http://localhost:5000/links');
            setLinks(response.data);
        } catch (error) {
            console.error('Error fetching links:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <h1>Loading...</h1>;
    }

    return (
        <>
            <TopNav />
            <h1 className='links-header'>Links</h1>
            <table>
                <thead>
                    <tr>
                        <th>URL</th>
                        <th>Top-Level Domain</th>
                    </tr>
                </thead>
                <tbody>
                    {links.map((link) => (
                        <tr key={link.link_id}>
                            <td>{link.url_link}</td>
                            <td>{link.tld}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
};

export default Links;

