import React from "react";
import { YoutubeSearchedFor } from "./YoutubeSearchedFor";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "YoutubeSearchedFor",
  component: YoutubeSearchedFor,
};

export const Default = () => <YoutubeSearchedFor />;
export const Fill = () => <YoutubeSearchedFor fill="blue" />;
export const Size = () => <YoutubeSearchedFor height="50" width="50" />;
export const CustomStyle = () => <YoutubeSearchedFor style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <YoutubeSearchedFor className="custom-class" />;
export const Clickable = () => <YoutubeSearchedFor onClick={()=>console.log("clicked")} />;

const Template = (args) => <YoutubeSearchedFor {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

