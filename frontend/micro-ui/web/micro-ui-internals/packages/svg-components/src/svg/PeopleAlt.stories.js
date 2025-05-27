import React from "react";
import { PeopleAlt } from "./PeopleAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PeopleAlt",
  component: PeopleAlt,
};

export const Default = () => <PeopleAlt />;
export const Fill = () => <PeopleAlt fill="blue" />;
export const Size = () => <PeopleAlt height="50" width="50" />;
export const CustomStyle = () => <PeopleAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PeopleAlt className="custom-class" />;

export const Clickable = () => <PeopleAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <PeopleAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
