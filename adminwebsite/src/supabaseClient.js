// src/supabaseClient.js
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://xyhhazbjkgtzcldfdbex.supabase.co'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5aGhhemJqa2d0emNsZGZkYmV4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU5MjU2NjUsImV4cCI6MjA2MTUwMTY2NX0.DvtCkvDVDe-bSXlaaaqMPhalF7FHuwib-1yoTj4DylU'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
