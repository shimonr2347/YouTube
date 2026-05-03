import React from 'react';
import { useState } from 'react';
import './Search.css';

function Search({ doSearch }) {
  const [query, setQuery] = useState('');

  const handleSearch = (e) => {
    e.preventDefault();
    doSearch(query);
  };

  return (
    <form className="search-box" role="search" onSubmit={handleSearch}>
      <input
        type="text"
        className="search-input"
        placeholder="Search videos..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      <button className="search-button " type="submit">
        <i className="bi bi-search"></i>
      </button>
    </form>
  );
}

export default Search;

