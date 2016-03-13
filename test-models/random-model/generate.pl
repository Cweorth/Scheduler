#!/usr/bin/perl

use strict;
use warnings;
use utf8;

use Data::Dumper;
use Data::RandomPerson;

my $IDSEQ = 0;
my $RPGEN = Data::RandomPerson->new();

sub makecsv {
    my ($fn, $header, $data) = @_;

    open(my $file, ">$fn")
        or die("Could not open $fn.");

    print $file (join("\t", @$header), "\n");

    foreach my $row (@$data) {
        if (grep { $_ eq "id" } (@$header)) {
            $row->{id} = $IDSEQ++;
        }

        my @data = @{$row}{@$header};
        print $file (join("\t", @data), "\n");
    }

    close($file);
}

# FIELDS
my $hfields = [ qw|id code name| ];
my $dfields = [
    { code => "TEST", name => "Experimental Informatics" },
];

makecsv("fields.csv", $hfields, $dfields);

# TEACHERS

my $hteachers = [ qw|id prefixTitles name surname suffixTitles| ];
my $dteachers = [];

foreach my $i (1..10) {
    my $p = $RPGEN->create();

    push(@$dteachers, {
            prefixTitles => "doc.", suffixTitles => "Ph.D.", 
            name => $p->{firstname}, surname => $p->{lastname}
        });
}

makecsv("teachers.csv", $hteachers, $dteachers);

# THESES
my $htheses = [ qw|id supervisor opponents name| ];
my $dtheses = [];

foreach my $i (1..16) {
    my $thesis = {
        supervisor => $dteachers->[int(rand(10))]->{id},
        opponents  => $dteachers->[int(rand(10))]->{id},
        name       => `fortune -s -n 30 | tr '\n' ' ' | tr -s ' '`,
    };
    
    ($thesis->{supervisor} += 1) %= 10 if ($thesis->{supervisor} eq $thesis->{opponents});

    push(@$dtheses, $thesis);
}

makecsv("theses.csv", $htheses, $dtheses);

# STUDENTS

my $hstudents = [ @$hteachers, qw|field repetition thesis examLevel| ];
my $dstudents = [];

foreach my $i (1..16) {
    my $p = $RPGEN->create();

    push(@$dstudents, {
            prefixTitles => "", suffixTitles => "", 
            name => $p->{firstname}, surname => $p->{lastname},
            field => "TEST", repetition => "NOTHING",
            thesis => $dtheses->[$i - 1]->{id}, examLevel => "BACHELOR"
        });
}

makecsv("students.csv", $hstudents, $dstudents);
