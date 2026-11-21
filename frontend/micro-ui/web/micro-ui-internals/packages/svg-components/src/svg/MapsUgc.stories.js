import React from "react";
import { MapsUgc } from "./MapsUgc";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MapsUgc",
  component: MapsUgc,
};

export const Default = () => <MapsUgc />;
export const Fill = () => <MapsUgc fill="blue" />;
export const Size = () => <MapsUgc height="50" width="50" />;
export const CustomStyle = () => <MapsUgc style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MapsUgc className="custom-class" />;

export const Clickable = () => <MapsUgc onClick={()=>console.log("clicked")} />;

const Template = (args) => <MapsUgc {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
